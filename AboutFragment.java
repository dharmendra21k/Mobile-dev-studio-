package com.mobiledev.androidstudio.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobiledev.androidstudio.BuildConfig;
import com.mobiledev.androidstudio.R;

/**
 * Fragment for the About screen
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        
        // Set version information
        TextView versionTextView = view.findViewById(R.id.text_version);
        String versionName = BuildConfig.VERSION_NAME;
        int versionCode = BuildConfig.VERSION_CODE;
        versionTextView.setText(String.format("Version %s (%d)", versionName, versionCode));
        
        // Button handlers
        Button licensesButton = view.findViewById(R.id.btn_licenses);
        Button githubButton = view.findViewById(R.id.btn_github);
        Button feedbackButton = view.findViewById(R.id.btn_feedback);
        
        licensesButton.setOnClickListener(v -> openLicenses());
        githubButton.setOnClickListener(v -> openGitHub());
        feedbackButton.setOnClickListener(v -> sendFeedback());
        
        return view;
    }
    
    /**
     * Open the open source licenses
     */
    private void openLicenses() {
        // TODO: Implement license display
        // For now, just prepare the URL for a potential future web view
        String licenseUrl = "https://github.com/yourusername/mobiledev/blob/main/LICENSE";
        openUrl(licenseUrl);
    }
    
    /**
     * Open the GitHub repository
     */
    private void openGitHub() {
        String githubUrl = "https://github.com/yourusername/mobiledev";
        openUrl(githubUrl);
    }
    
    /**
     * Send feedback via email
     */
    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:feedback@yourdomain.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Mobile Dev Studio Feedback");
        
        String appVersion = BuildConfig.VERSION_NAME;
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + 
                " (Android " + android.os.Build.VERSION.RELEASE + ")";
        
        String emailBody = "\n\n--\nApp version: " + appVersion + "\nDevice: " + deviceInfo;
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    
    /**
     * Open a URL in the browser
     *
     * @param url URL to open
     */
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}