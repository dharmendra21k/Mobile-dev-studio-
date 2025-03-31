package com.mobiledev.androidstudio.models;

public class Template {
    private String name;
    private String description;
    private String resourceId;
    private int templateType;
    
    // Template types
    public static final int TYPE_SIMPLE_APP = 1;
    public static final int TYPE_LIST_VIEW = 2;
    public static final int TYPE_DATABASE = 3;
    
    public Template(String name, String description, String resourceId, int templateType) {
        this.name = name;
        this.description = description;
        this.resourceId = resourceId;
        this.templateType = templateType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public int getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Template template = (Template) obj;
        return resourceId.equals(template.resourceId);
    }
    
    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }
}