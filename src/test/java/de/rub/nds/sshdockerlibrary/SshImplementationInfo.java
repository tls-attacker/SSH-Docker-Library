package de.rub.nds.sshdockerlibrary;

import java.util.*;

public class SshImplementationInfo {

    private String implementationName;
    private String softwareVersionBanner;
    private List<String> cipher;
    private List<String> compression;
    private List<String> hostkey;
    private List<String> kex;
    private List<String> mac;
    private List<String> auth;
    private List<String> extensions;


    public SshImplementationInfo(){
        this.cipher = new ArrayList<>();
        this.compression = new ArrayList<>();
        this.hostkey = new ArrayList<>();
        this.kex = new ArrayList<>();
        this.mac = new ArrayList<>();
        this.auth = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public SshImplementationInfo(String pImplementationName, String pSoftwareVersionBanner, List<String> pCipher, List<String> pCompression, List<String> pHostkey, List<String> pKex, List<String> pMac, List<String> pAuth, List<String> pExtensions){
        this.implementationName= pImplementationName;
        this.softwareVersionBanner= pSoftwareVersionBanner;
        this.cipher = pCipher;
        this.compression = pCompression;
        this.hostkey = pHostkey;
        this.kex = pKex;
        this.mac = pMac;
        this.auth = pAuth;
        this.extensions = pExtensions;
    }


    public String getImplementationName() {
        return implementationName;
    }

    public void setImplementationName(String implementationName) {
        this.implementationName = implementationName;
    }

    public String getSoftwareVersionBanner() {
        return softwareVersionBanner;
    }

    public void setSoftwareVersionBanner(String softwareVersionBanner) {
        this.softwareVersionBanner = softwareVersionBanner;
    }

    public List<String> getCipher() {
        return cipher;
    }

    public void setCipher(List<String> cipher) {
        this.cipher = cipher;
    }

    public List<String> getCompression() {
        return compression;
    }

    public void setCompression(List<String> compression) {
        this.compression = compression;
    }

    public List<String> getHostkey() {
        return hostkey;
    }

    public void setHostkey(List<String> hostkey) {
        this.hostkey = hostkey;
    }

    public List<String> getKex() {
        return kex;
    }

    public void setKex(List<String> kex) {
        this.kex = kex;
    }

    public List<String> getMac() {
        return mac;
    }

    public void setMac(List<String> mac) {
        this.mac = mac;
    }

    public List<String> getAuth() {
        return auth;
    }

    public void setAuth(List<String> auth) {
        this.auth = auth;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }
}
