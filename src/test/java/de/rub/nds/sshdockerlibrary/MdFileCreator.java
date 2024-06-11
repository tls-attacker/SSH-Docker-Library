package de.rub.nds.sshdockerlibrary;


import de.rub.nds.sshdockerlibrary.SshImplementationInfo;

import java.io.*;
import java.util.ArrayList;

public class MdFileCreator {

    /**
     * This method should be used to automatically create a new markdown file, including the new scan results.
     * General information about implementations shouldn`t be changed, just the protocol support summary
     */
    public void recreateMdFile(SshImplementationInfo pSshImplementationInfo){
        String filePath = "ssh-comparison/_impls";
        String sourceFilePath = filePath+"/"+ ImplementationMapping.getByImplementationName(pSshImplementationInfo.getImplementationName().split("-")[0]); // Path to the source file

        filePath=filePath+"/"+pSshImplementationInfo.getImplementationName()+ ".md";
        String sshScanReport = generateMarkdown(pSshImplementationInfo);
        System.out.print(sshScanReport);

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // Skip the first two lines
            reader.readLine();
            reader.readLine();
            writer.write("---");
            writer.newLine();
            writer.write("title: "+pSshImplementationInfo.getImplementationName());
            writer.newLine();
            // Copy the next lines until protocols
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("protocols:")) {
                    break;
                }
                writer.write(line);
                writer.newLine();
            }
            writer.newLine();
            writer.write(sshScanReport);
            writer.write("---");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String generateMarkdown(SshImplementationInfo info) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("protocols:\n");
        appendList(markdown, "cipher", (ArrayList<String>) info.getCipher());
        appendList(markdown, "compression", (ArrayList<String>) info.getCompression());
        appendList(markdown, "hostkey", (ArrayList<String>) info.getHostkey());
        appendList(markdown, "kex", (ArrayList<String>) info.getKex());
        appendList(markdown, "mac", (ArrayList<String>) info.getMac());
        appendList(markdown, "userauth", (ArrayList<String>) info.getAuth());
        return markdown.toString();
    }

    private static void appendList(StringBuilder markdown, String key, ArrayList<String> items) {
        markdown.append("    ").append(key).append(":\n");
        for (String item : items) {
            markdown.append("        - ").append(item).append("\n");
        }
    }



}
