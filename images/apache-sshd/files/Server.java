// Minimal SSH server built on Apache MINA SSHD.
import java.nio.file.Paths;
import java.util.List;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractiveFactory;

public final class Server {
    public static void main(String[] args) throws Exception {
        String user = System.getenv().getOrDefault("SSH_USERNAME", "sshattacker");
        String pass = System.getenv().getOrDefault("SSH_PASSWORD", "secret");

        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(Integer.parseInt(System.getenv().getOrDefault("SSH_PORT", "22")));
        sshd.setKeyPairProvider(
                new SimpleGeneratorHostKeyProvider(Paths.get("/app/hostkey.ser")));
        sshd.setPasswordAuthenticator((u, p, s) -> user.equals(u) && pass.equals(p));

        List<AuthorizedKeyEntry> entries =
                AuthorizedKeyEntry.readAuthorizedKeys(Paths.get("/app/authorized_keys"));
        sshd.setPublickeyAuthenticator((u, key, s) -> {
            for (AuthorizedKeyEntry e : entries) {
                try {
                    if (key.equals(e.resolvePublicKey(s, null, PublicKeyEntryResolver.IGNORING))) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // an unparsable entry is simply not a match
                }
            }
            return false;
        });
        sshd.setShellFactory(new ProcessShellFactory("/bin/sh", "/bin/sh", "-i", "-l"));
        sshd.start();
        System.out.println("listening on " + sshd.getPort());
        Thread.currentThread().join();
    }
}
