package com.mobiledev.androidstudio.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.utils.PerformanceOptimizer;

/**
 * Fragment for application settings
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_KEY_THEME = "theme";
    private static final String PREF_KEY_FONT_SIZE = "font_size";
    private static final String PREF_KEY_CLOUD_COMPILE = "cloud_compile";
    private static final String PREF_KEY_STORAGE_PATH = "storage_path";

    private PerformanceOptimizer performanceOptimizer;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        // Initialize PerformanceOptimizer
        performanceOptimizer = new PerformanceOptimizer(requireContext());
        
        // Set summaries for list preferences
        updateSummaries();
        
        // Disable cloud compilation if device doesn't meet requirements
        if (!performanceOptimizer.meetsMinimumRequirements()) {
            SwitchPreferenceCompat cloudCompilePreference = findPreference(PREF_KEY_CLOUD_COMPILE);
            if (cloudCompilePreference != null) {
                cloudCompilePreference.setChecked(true);
                cloudCompilePreference.setEnabled(false);
                cloudCompilePreference.setSummary(
                        "Cloud compilation is required on this device due to hardware limitations");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the listener
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_THEME)) {
            // Apply theme change
            String theme = sharedPreferences.getString(PREF_KEY_THEME, "system");
            applyTheme(theme);
        }
        
        // Update preference summaries
        updateSummaries();
    }

    /**
     * Update preference summaries to show current values
     */
    private void updateSummaries() {
        ListPreference themePreference = findPreference(PREF_KEY_THEME);
        if (themePreference != null) {
            themePreference.setSummary(themePreference.getEntry());
        }
        
        ListPreference fontSizePreference = findPreference(PREF_KEY_FONT_SIZE);
        if (fontSizePreference != null) {
            fontSizePreference.setSummary(fontSizePreference.getEntry());
        }
        
        Preference storagePathPreference = findPreference(PREF_KEY_STORAGE_PATH);
        if (storagePathPreference != null) {
            storagePathPreference.setSummary(
                    "Current path: " + requireContext().getExternalFilesDir(null).getAbsolutePath());
        }
    }

    /**
     * Apply the selected theme
     *
     * @param theme Theme setting value (light, dark, or system)
     */
    private void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default: // system
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}