package com.mobiledev.androidstudio.installation;

/**
 * Class representing a package
 */
public class Package {

    private String packageName;
    private String name;
    private String description;
    private String command;
    private boolean recommended;
    private String executableName;
    private boolean installed;

    /**
     * Constructor
     *
     * @param packageName Package name
     * @param name Display name
     * @param description Description
     * @param command Command to check if installed
     * @param recommended True if recommended
     * @param executableName Name of the executable
     */
    public Package(String packageName, String name, String description, String command, boolean recommended, String executableName) {
        this.packageName = packageName;
        this.name = name;
        this.description = description;
        this.command = command;
        this.recommended = recommended;
        this.executableName = executableName;
        this.installed = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public String getExecutableName() {
        return executableName;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}