package de.rub.nds.sshdockerlibrary;

import de.rub.nds.scanner.core.probe.result.ListResult;
import de.rub.nds.scanner.core.probe.result.StringResult;
import de.rub.nds.scanner.core.probe.result.TestResult;
import de.rub.nds.scanner.core.probe.result.TestResults;
import de.rub.nds.scanner.core.report.ScanReport;
import de.rub.nds.sshscanner.core.constants.SshAnalyzedProperty;
import de.rub.nds.sshscanner.core.constants.SshProbeType;
import de.rub.nds.sshscanner.serverscanner.config.SshServerScannerConfig;
import de.rub.nds.sshscanner.serverscanner.execution.SshServerScanner;
import junit.extensions.TestSetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;


/**
 * Unit test
 */
@Execution(ExecutionMode.SAME_THREAD)
public class ServerImagesTest {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String logging=System.getenv("LOGGING");
    private static Map<String, String> failedImages= new HashMap<String, String>();

    /**
     * Takes the current compose.yml file and collects all server instances and maps them to the corresponding port on the localhost.
     */
    public static Stream<Arguments> scanComposeFile() {
        Yaml yaml = new Yaml();
        File file = new File("compose.yml");
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(inputStream);

            String version = (String) data.get("version");
            System.out.println("Version: " + version);

            /*
            * asyncssh-server
bitvise-server
dropbear-server
erlang-ssh-server
libssh-mbedtls-server
libssh-server
openssh-server
russh-server
sshd-lite-server
tinyssh-server
wolfssh-server*/
            Map<String, Map<String, Object>> services = (Map<String, Map<String, Object>>) data.get("services");
            return services.entrySet().stream()
                    .filter(entry -> entry.getKey().contains("wolfssh-server-1.4.14"))
                    .flatMap(entry -> {
                        String serviceName = entry.getKey();
                        Map<String, Object> serviceDetails = entry.getValue();
                        String ports = serviceDetails.get("ports").toString();
                        int port = Integer.parseInt(ports.substring(1, ports.length() - 1).split(":")[0]);
                        return Stream.of(Arguments.of(serviceName, port));
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }


    //@BeforeAll
    @AfterAll
    public static void reinitializeComposeEnv() {
        try {
            String[] command = {"docker-compose", "down"};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if(failedImages.size()>0 &&logging.equals("true")){
            try {
                File logFile = new File("logs","log_"+ System.currentTimeMillis());
                FileWriter myWriter = new FileWriter(logFile);
                myWriter.write("\n The following images failed to scan: ");
                System.out.println("\n\n\n"+"\n The following images failed to scan: ");
                for (Map.Entry<String, String> entry : failedImages.entrySet()) {
                    System.out.println("\n"+entry.getKey() + ": " + entry.getValue());
                    myWriter.write("\n"+entry.getKey() + ": " + entry.getValue());
                }
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @ParameterizedTest
    @MethodSource("scanComposeFile")
    public void testServerImage(String serviceName, Integer port) throws Exception {
        SshServerScannerConfig config = new SshServerScannerConfig();
        //Hoststring format: "localhost:22"
        config.getClientDelegate().setHost("localhost:" + port.toString());

        try {
            // Command to run docker-compose up
            //docker compose up openssh-server-7.2p2 --build --detach
            String[] command = {"docker-compose", "up", serviceName, "--build","--detach", "--wait"};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Docker Compose Up process exited with code " + exitCode);
            if(exitCode!=0){
                failedImages.put(serviceName, "Docker compose Up exit code is not zero:"+ exitCode);
            }
            Assertions.assertEquals(0, exitCode, "Docker compose Up exit code is not 0 as expected");


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if(serviceName.contains("erlang-ssh-server")){
            //i didn't find the origin of why erlang-ssh-servers are not ready right away,
            // but this workaround makes the server socket waits for the server socket to be available
            sleep(1000);
        }
        long scanStart = System.currentTimeMillis();
        config.getExecutorConfig().setProbes(SshProbeType.VERSION, SshProbeType.KEX_NAME_LISTS, SshProbeType.EXTENSIONS, SshProbeType.AUTHENTICATION_METHODS, SshProbeType.PUBKEY_AUTHENTICATION);
        ScanReport scanReport;
        LOGGER.info("Performing scan, this may take some time...");
        try (SshServerScanner scanner = new SshServerScanner(config)) {
            scanReport = scanner.scan();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            failedImages.put(serviceName, "Exception during scan: "+e.getMessage()+ "\n"+ exceptionAsString);
            throw e;
        }

        try {
            String[] command = {"docker-compose", "down", serviceName};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Docker Compose Down process exited with code " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }



        LOGGER.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        //ssh version
        String softwareVersion = null;
        if(scanReport.isProbeAlreadyExecuted(SshProbeType.VERSION)){
            softwareVersion = (String) ((StringResult) scanReport.getResult(SshAnalyzedProperty.SOFTWARE_VERSION)).getValue();
        }

        softwareVersion=(String) ((StringResult) scanReport.getResult(SshAnalyzedProperty.SOFTWARE_VERSION)).getValue();
        LOGGER.info("Scanned ssh version: " + softwareVersion);


        //encryption algs
        List scannedEncryptionAlgs = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_ENCRYPTION_ALGORITHMS_SERVER_TO_CLIENT)).getList();
        LOGGER.info("Scanned compression algorithms: " + scannedEncryptionAlgs.toString());

        //compression algs
        List scannedCompressionAlgs = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_COMPRESSION_ALGORITHMS_SERVER_TO_CLIENT)).getList();
        LOGGER.info("Scanned compression algorithms: " + scannedCompressionAlgs.toString());


        //hostkey algs
        List scannedHostkeyAlgs = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_SERVER_HOST_KEY_ALGORITHMS)).getList();
        LOGGER.info("Scanned hostkey algorithms: " + scannedHostkeyAlgs.toString());


        //kex algs
        List scannedKexAlgs = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_KEY_EXCHANGE_ALGORITHMS)).getList();
        LOGGER.info("Scanned kex algorithms: " + scannedKexAlgs.toString());

        //mac algs
        List scannedMacAlgs = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_MAC_ALGORITHMS_SERVER_TO_CLIENT)).getList();
        LOGGER.info("Scanned mac algorithms: " + scannedMacAlgs.toString());

        //check for pubkey auth method offer

        List scannedAuthMethods= new ArrayList();
        String test= scanReport.getResult(SshAnalyzedProperty.SUPPORTED_AUTHENTICATION_METHODS).getType();
        if(scanReport.getResult(SshAnalyzedProperty.SUPPORTED_AUTHENTICATION_METHODS).getType().equals("TestResults") &&
                scanReport.getResult(SshAnalyzedProperty.SUPPORTED_AUTHENTICATION_METHODS).equalsExpectedResult(TestResults.COULD_NOT_TEST)){
            scannedAuthMethods.add("not extractable");
            failedImages.put(serviceName, "No UserAuthFailureMessage send");
        }else {
            scannedAuthMethods = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_AUTHENTICATION_METHODS)).getList();
            LOGGER.info("Scanned auth mehotds: " + scannedAuthMethods.toString());
        }


        //extensions
        List scannedExtensions = ((ListResult) scanReport.getResult(SshAnalyzedProperty.SUPPORTED_MAC_ALGORITHMS_SERVER_TO_CLIENT)).getList();
        LOGGER.info("Scanned extensions: " + scannedExtensions.toString());

        SshImplementationInfo scannedImplementationInfo = new SshImplementationInfo(serviceName, softwareVersion,
                scannedEncryptionAlgs, scannedCompressionAlgs, scannedHostkeyAlgs, scannedKexAlgs, scannedMacAlgs, scannedAuthMethods, scannedExtensions);
        MdFileCreator mdFileCreator = new MdFileCreator();
        mdFileCreator.recreateMdFile(scannedImplementationInfo);

        //pubkey authentication
        TestResult pubkeyProbeResult=scanReport.getResult(SshAnalyzedProperty.SUPPORTS_PUBKEY_AUTHENTICATION);
        Assertions.assertTrue(pubkeyProbeResult.equalsExpectedResult(TestResults.TRUE),"Scanned server image successfully performs publickey auth ");
        if(pubkeyProbeResult.equalsExpectedResult(TestResults.TRUE)){
            LOGGER.info("Successfully performed publickey auth on the scanned image");
        }




    }
}


