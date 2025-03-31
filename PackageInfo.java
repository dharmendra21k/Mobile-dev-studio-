package com.mobiledev.androidstudio.installation;

/**
 * Class that represents a installable package
 */
public class PackageInfo {
    private String name;
    private String displayName;
    private String description;
    private String installCommand;
    private boolean installed;
    private boolean installing;
    
    public PackageInfo(String name, String displayName, String description, String installCommand) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.installCommand = installCommand;
        this.installed = false;
        this.installing = false;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getInstallCommand() {
        return installCommand;
    }
    
    public boolean isInstalled() {
        return installed;
    }
    
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
    
    public boolean isInstalling() {
        return installing;
    }
    
    public void setInstalling(boolean installing) {
        this.installing = installing;
    }
}