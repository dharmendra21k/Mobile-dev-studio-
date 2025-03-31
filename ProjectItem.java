package com.mobiledev.androidstudio.build;

/**
 * Class representing a project item
 */
public class ProjectItem {

    private String name;
    private String path;

    /**
     * Constructor
     *
     * @param name Project name
     * @param path Project path
     */
    public ProjectItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return name;
    }
}