// Minimal SSH client built on SSH.NET.
using Renci.SshNet;

class Program
{
    static int Main(string[] args)
    {
        string host = "127.0.0.1", user = "sshattacker", pass = "secret",
               cmd = "true", key = null;
        int port = 22;
        for (int i = 0; i + 1 < args.Length; i += 2)
        {
            switch (args[i])
            {
                case "-h": host = args[i + 1]; break;
                case "-p": port = int.Parse(args[i + 1]); break;
                case "-l": user = args[i + 1]; break;
                case "-P": pass = args[i + 1]; break;
                case "-i": key = args[i + 1]; break;
                case "-c": cmd = args[i + 1]; break;
            }
        }

        ConnectionInfo info = key != null
            ? new ConnectionInfo(host, port, user, new PrivateKeyAuthenticationMethod(user, new PrivateKeyFile(key)))
            : new ConnectionInfo(host, port, user, new PasswordAuthenticationMethod(user, pass));

        using var client = new SshClient(info);
        client.Connect();
        if (!client.IsConnected) { System.Console.Error.WriteLine("not connected"); return 1; }
        System.Console.WriteLine(client.ConnectionInfo.ServerVersion);
        System.Console.Write(client.RunCommand(cmd).Result);
        client.Disconnect();
        return 0;
    }
}
