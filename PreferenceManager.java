package com.mobiledev.androidstudio.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing shared preferences
 */
public class PreferenceManager {
    
    private static final String PREF_NAME = "mobile_dev_studio_prefs";
    
    private final SharedPreferences sharedPreferences;
    
    /**
     * Constructor
     * 
     * @param context Context
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Save a string value
     * 
     * @param key   Key
     * @param value Value
     */
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * Get a string value
     * 
     * @param key          Key
     * @param defaultValue Default value
     * @return Value
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
    
    /**
     * Save an integer value
     * 
     * @param key   Key
     * @param value Value
     */
    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * Get an integer value
     * 
     * @param key          Key
     * @param defaultValue Default value
     * @return Value
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
    
    /**
     * Save a boolean value
     * 
     * @param key   Key
     * @param value Value
     */
    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * Get a boolean value
     * 
     * @param key          Key
     * @param defaultValue Default value
     * @return Value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    /**
     * Remove a value
     * 
     * @param key Key
     */
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
    
    /**
     * Clear all preferences
     */
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * Check if a key exists
     * 
     * @param key Key
     * @return True if the key exists
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}