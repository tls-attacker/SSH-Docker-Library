extern crate thrussh;
extern crate thrussh_keys;
extern crate futures;
extern crate tokio;
extern crate env_logger;
use std::env;
use std::sync::Arc;
use thrussh::*;
use thrussh::server::{Auth, Session};
use thrussh_keys::*;
use futures::Future;
use std::io::Read;


struct Client {
}

impl client::Handler for Client {
   type Error = anyhow::Error;
   type FutureUnit = futures::future::Ready<Result<(Self, client::Session), anyhow::Error>>;
   type FutureBool = futures::future::Ready<Result<(Self, bool), anyhow::Error>>;

   fn finished_bool(self, b: bool) -> Self::FutureBool {
       futures::future::ready(Ok((self, b)))
   }
   fn finished(self, session: client::Session) -> Self::FutureUnit {
       futures::future::ready(Ok((self, session)))
   }
   fn check_server_key(self, server_public_key: &key::PublicKey) -> Self::FutureBool {
       println!("check_server_key: {:?}", server_public_key);
       self.finished_bool(true)
   }
   fn channel_open_confirmation(self, channel: ChannelId, max_packet_size: u32, window_size: u32, session: client::Session) -> Self::FutureUnit {
       println!("channel_open_confirmation: {:?}", channel);
       self.finished(session)
   }
   fn data(self, channel: ChannelId, data: &[u8], session: client::Session) -> Self::FutureUnit {
       println!("data on channel {:?}: {:?}", channel, std::str::from_utf8(data));
       self.finished(session)
   }
}

#[tokio::main]
async fn main() {
    let args: Vec<String> = env::args().skip(1).collect();
    let mut _connect_to= String::from("localhost:22");
    let mut _username = String::from("sshattacker");
    if args.len()>=1 {
        _connect_to=String::from(&args[0]);
    }
    if args.len()==2 {
        _username =String::from(&args[1]);
    }

    let config = thrussh::client::Config::default();
    let config = Arc::new(config);
    let sh = Client{};

    //load all client keys
    let keypair_ed25519=thrussh_keys::load_secret_key("/thrussh/keys/id_ed25519", None).unwrap();
    let keypair_dsa=thrussh_keys::load_secret_key("/thrussh/keys/id_dsa", None).unwrap();
    let keypair_ecdsa=thrussh_keys::load_secret_key("/thrussh/keys/id_ecdsa", None).unwrap();
    let keypair_rsa=thrussh_keys::load_secret_key("/thrussh/keys/id_rsa", None).unwrap();

    let mut agent = thrussh_keys::agent::client::AgentClient::connect_env().await.unwrap();
    agent.add_identity(&keypair_ed25519, &[]).await.unwrap();
    agent.add_identity(&keypair_dsa, &[]).await.unwrap();
    agent.add_identity(&keypair_ecdsa, &[]).await.unwrap();
    agent.add_identity(&keypair_rsa, &[]).await.unwrap();
    println!("Starting connection on {}", &_connect_to);
    let mut session = thrussh::client::connect(config, &_connect_to, sh).await.unwrap();
    println!("Public-key authentication as {} result:{}",&_username, session.authenticate_future(&_username, keypair_ed25519.clone_public_key(), agent).await.1.unwrap());
    
    let mut channel = session.channel_open_session().await.unwrap();
    
    if let Some(msg) = channel.wait().await {
        println!("{:?}", msg);
    }   
    channel.request_pty(true, "vt100", 80, 24, 640, 430, &[(Pty::ECHO, 019000u32)]).await.unwrap();
    if let Some(msg) = channel.wait().await {
        println!("{:?}", msg)
    }
}