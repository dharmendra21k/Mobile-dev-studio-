package com.mobiledev.androidstudio.cloud;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for cloud compilation
 */
public class CloudCompilationConfig {
    
    public static final String BUILD_TYPE_DEBUG = "debug";
    public static final String BUILD_TYPE_RELEASE = "release";
    
    public static final String DEPLOYMENT_NONE = "none";
    public static final String DEPLOYMENT_PLAY_STORE = "play_store";
    public static final String DEPLOYMENT_FIREBASE = "firebase";
    public static final String DEPLOYMENT_CUSTOM = "custom";
    
    private String buildType;
    private String deploymentType;
    private Map<String, String> extraOptions;
    private String apiKey;
    
    /**
     * Constructor
     */
    public CloudCompilationConfig() {
        this.buildType = BUILD_TYPE_DEBUG;
        this.deploymentType = DEPLOYMENT_NONE;
        this.extraOptions = new HashMap<>();
    }
    
    /**
     * Get build type
     * 
     * @return Build type
     */
    public String getBuildType() {
        return buildType;
    }
    
    /**
     * Set build type
     * 
     * @param buildType Build type
     */
    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }
    
    /**
     * Get deployment type
     * 
     * @return Deployment type
     */
    public String getDeploymentType() {
        return deploymentType;
    }
    
    /**
     * Set deployment type
     * 
     * @param deploymentType Deployment type
     */
    public void setDeploymentType(String deploymentType) {
        this.deploymentType = deploymentType;
    }
    
    /**
     * Get extra options
     * 
     * @return Extra options
     */
    public Map<String, String> getExtraOptions() {
        return extraOptions;
    }
    
    /**
     * Set extra options
     * 
     * @param extraOptions Extra options
     */
    public void setExtraOptions(Map<String, String> extraOptions) {
        this.extraOptions = extraOptions;
    }
    
    /**
     * Add extra option
     * 
     * @param key Key
     * @param value Value
     */
    public void addExtraOption(String key, String value) {
        this.extraOptions.put(key, value);
    }
    
    /**
     * Get API key
     * 
     * @return API key
     */
    public String getApiKey() {
        return apiKey;
    }
    
    /**
     * Set API key
     * 
     * @param apiKey API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Convert build type index to string
     * 
     * @param index Build type index
     * @return Build type string
     */
    public static String buildTypeFromIndex(int index) {
        switch (index) {
            case 0:
                return BUILD_TYPE_DEBUG;
            case 1:
                return BUILD_TYPE_RELEASE;
            default:
                return BUILD_TYPE_DEBUG;
        }
    }
    
    /**
     * Convert build type string to index
     * 
     * @param buildType Build type string
     * @return Build type index
     */
    public static int buildTypeToIndex(String buildType) {
        if (BUILD_TYPE_RELEASE.equals(buildType)) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Convert deployment type index to string
     * 
     * @param index Deployment type index
     * @return Deployment type string
     */
    public static String deploymentTypeFromIndex(int index) {
        switch (index) {
            case 0:
                return DEPLOYMENT_NONE;
            case 1:
                return DEPLOYMENT_PLAY_STORE;
            case 2:
                return DEPLOYMENT_FIREBASE;
            case 3:
                return DEPLOYMENT_CUSTOM;
            default:
                return DEPLOYMENT_NONE;
        }
    }
    
    /**
     * Convert deployment type string to index
     * 
     * @param deploymentType Deployment type string
     * @return Deployment type index
     */
    public static int deploymentTypeToIndex(String deploymentType) {
        if (DEPLOYMENT_PLAY_STORE.equals(deploymentType)) {
            return 1;
        } else if (DEPLOYMENT_FIREBASE.equals(deploymentType)) {
            return 2;
        } else if (DEPLOYMENT_CUSTOM.equals(deploymentType)) {
            return 3;
        }
        return 0;
    }
}