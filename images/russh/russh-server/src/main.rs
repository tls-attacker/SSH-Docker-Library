use std::collections::HashMap;
use std::pin::Pin;
use std::str::FromStr;
use std::sync::Arc;

use std::path::PathBuf;
use std::fs::File;
use std::io::Read;

use futures::FutureExt;
use russh::server::{Auth, Session};
use russh::*;
use russh_keys::*;
use tokio::sync::Mutex;

use clap::Parser;

/// Simple SSH server written in Rust.
///
/// Based on the `echoserver` example:
///
/// - <https://github.com/warp-tech/russh/blob/v0.34.0/russh/examples/echoserver.rs>
#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
   /// Address to bind to.
   #[arg(short, long, default_value = "0.0.0.0")]
   address: String,

   /// Port number to listen on.
   #[arg(short, long, default_value_t = 22)]
   port: u16,

   /// Use a fixed host key.
   ///
   /// This is a serialized ed25519 key pair from the `ed25519-dalek` crate. See
   /// <https://docs.rs/ed25519-dalek/1.0.1/ed25519_dalek/struct.Keypair.html> for details.
   #[arg(short, long)]
   key: Option<PathBuf>,
}

#[tokio::main]
async fn main() {
    env_logger::builder()
        .filter_level(log::LevelFilter::Debug)
        .init();

    let args = Args::parse();

    let mut config = russh::server::Config::default();
    config.connection_timeout = Some(std::time::Duration::from_secs(3600));
    config.auth_rejection_time = std::time::Duration::from_secs(3);

    let host_key = if let Some(path) = args.key {
        log::debug!("Reading host key from {}...", path.display());
        let mut f = File::open(path).unwrap();
        let mut buffer: [u8; 64] = [0; 64];
        f.read_exact(&mut buffer).unwrap();
        let kp = ed25519_dalek::Keypair::from_bytes(&buffer).unwrap();
        russh_keys::key::KeyPair::Ed25519(kp)
    } else {
        log::debug!("Generating new host key...");
        russh_keys::key::KeyPair::generate_ed25519().unwrap()
    };

    config
        .keys
        .push(host_key);


    let config = Arc::new(config);
    let sh = Server {
        clients: Arc::new(Mutex::new(HashMap::new())),
        id: 0,
    };
    let addr_string = format!("{}:{}", args.address, args.port);
    log::info!("Listening on {}...", addr_string);
    russh::server::run(
        config,
        &std::net::SocketAddr::from_str(&addr_string).unwrap(),
        sh,
    )
    .await
    .unwrap();
}

#[derive(Clone)]
struct Server {
    clients: Arc<Mutex<HashMap<(usize, ChannelId), russh::server::Handle>>>,
    id: usize,
}

impl Server {
    async fn post(&mut self, data: CryptoVec) {
        let mut clients = self.clients.lock().await;
        for ((id, channel), ref mut s) in clients.iter_mut() {
            if *id != self.id {
                let _ = s.data(*channel, data.clone()).await;
            }
        }
    }
}

impl server::Server for Server {
    type Handler = Self;
    fn new_client(&mut self, _: Option<std::net::SocketAddr>) -> Self {
        let s = self.clone();
        self.id += 1;
        s
    }
}

impl server::Handler for Server {
    type Error = anyhow::Error;
    type FutureAuth =
        Pin<Box<dyn core::future::Future<Output = anyhow::Result<(Self, Auth)>> + Send>>;
    type FutureUnit =
        Pin<Box<dyn core::future::Future<Output = anyhow::Result<(Self, Session)>> + Send>>;
    type FutureBool =
        Pin<Box<dyn core::future::Future<Output = anyhow::Result<(Self, Session, bool)>> + Send>>;

    fn finished_auth(self, auth: Auth) -> Self::FutureAuth {
        async { Ok((self, auth)) }.boxed()
    }

    fn finished_bool(self, b: bool, s: Session) -> Self::FutureBool {
        async move { Ok((self, s, b)) }.boxed()
    }

    fn finished(self, s: Session) -> Self::FutureUnit {
        async { Ok((self, s)) }.boxed()
    }

    fn channel_open_session(self, channel: ChannelId, session: Session) -> Self::FutureBool {
        async move {
            {
                let mut clients = self.clients.lock().await;
                clients.insert((self.id, channel), session.handle());
            }
            Ok((self, session, true))
        }
        .boxed()
    }

    fn auth_password(self, user: &str, password: &str) -> Self::FutureAuth {
        let result = if user == "sshattacker" && password == "secret" {
            server::Auth::Accept
        } else {
            server::Auth::Reject { proceed_with_methods: Some(MethodSet::all()) }
        };
        self.finished_auth(result)
    }

    fn auth_publickey(self, user: &str, k: &key::PublicKey) -> Self::FutureAuth {
        let result = if user == "sshattacker" {
            if k.fingerprint() == "SHA256:+r3jzFOCyyXhEFhjitsllvC4MTn7eCNlfa/4PYZMJ0s" {
                server::Auth::Accept
            } else {
                server::Auth::Reject { proceed_with_methods: Some(MethodSet::all()) }
            }
        } else {
            server::Auth::Reject { proceed_with_methods: Some(MethodSet::all()) }
        };
        self.finished_auth(result)
    }

    fn data(mut self, channel: ChannelId, data: &[u8], mut session: Session) -> Self::FutureUnit {
        let data = CryptoVec::from(format!("Got data: {}\r\n", String::from_utf8_lossy(data)));
        async move {
            {
                self.post(data.clone()).await;
            }
            session.data(channel, data);
            Ok((self, session))
        }
        .boxed()
    }

    fn tcpip_forward(self, address: &str, port: u32, session: Session) -> Self::FutureBool {
        let handle = session.handle();
        let address = address.to_string();
        tokio::spawn(async move {
            let mut channel = handle
                .channel_open_forwarded_tcpip(address, port, "1.2.3.4", 1234)
                .await
                .unwrap();
            let _ = channel.data(&b"Hello from a forwarded port"[..]).await;
            let _ = channel.eof().await;
        });
        self.finished_bool(true, session)
    }
}
