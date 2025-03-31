package com.mobiledev.androidstudio.models;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * Model class for Android projects
 */
public class Project implements Serializable {

    private String name;
    private String packageName;
    private String template;
    private String path;
    private long lastModified;
    private Date createdDate;

    /**
     * Create a new project
     */
    public Project() {
        this.createdDate = new Date();
    }

    /**
     * Create a new project with details
     *
     * @param name Project name
     * @param packageName Package name
     * @param template Project template
     * @param path Project path
     */
    public Project(String name, String packageName, String template, String path) {
        this();
        this.name = name;
        this.packageName = packageName;
        this.template = template;
        this.path = path;
        this.lastModified = System.currentTimeMillis();
    }

    /**
     * Get the project name
     *
     * @return Project name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the project name
     *
     * @param name Project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the package name
     *
     * @return Package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Set the package name
     *
     * @param packageName Package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get the project template
     *
     * @return Project template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Set the project template
     *
     * @param template Project template
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Get the project path
     *
     * @return Project path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the project path
     *
     * @param path Project path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the last modified timestamp
     *
     * @return Last modified timestamp
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Set the last modified timestamp
     *
     * @param lastModified Last modified timestamp
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get the created date
     *
     * @return Created date
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Set the created date
     *
     * @param createdDate Created date
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Check if the project directory exists
     *
     * @return true if the project exists
     */
    public boolean exists() {
        if (path == null) {
            return false;
        }
        
        File projectDir = new File(path);
        return projectDir.exists() && projectDir.isDirectory();
    }

    /**
     * Get project size in bytes
     *
     * @return Project size
     */
    public long getSize() {
        if (!exists()) {
            return 0;
        }
        
        return calculateDirectorySize(new File(path));
    }

    /**
     * Calculate directory size recursively
     *
     * @param directory Directory
     * @return Total size
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else if (file.isDirectory()) {
                size += calculateDirectorySize(file);
            }
        }
        
        return size;
    }

    /**
     * Update the last modified time
     */
    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        Project other = (Project) obj;
        
        if (path != null) {
            return path.equals(other.path);
        }
        
        return other.path == null;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}