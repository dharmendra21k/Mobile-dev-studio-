package com.mobiledev.androidstudio.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.mobiledev.androidstudio.MobileDevApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Class for managing development projects
 */
public class ProjectManager {
    private static final String TAG = "ProjectManager";
    private static final String PROJECTS_DIR = "projects";
    private static final String BACKUP_DIR = "backups";
    private static final String TEMPLATES_DIR = "templates";
    
    private final Context context;
    private final File projectsDir;
    private final File backupDir;
    private final File templatesDir;
    
    public ProjectManager(Context context) {
        this.context = context;
        
        // Create projects directory in app-specific storage
        this.projectsDir = new File(context.getFilesDir(), PROJECTS_DIR);
        if (!projectsDir.exists()) {
            if (projectsDir.mkdirs()) {
                Log.d(TAG, "Created projects directory");
            } else {
                Log.e(TAG, "Failed to create projects directory");
            }
        }
        
        // Create backup directory
        this.backupDir = new File(context.getFilesDir(), BACKUP_DIR);
        if (!backupDir.exists()) {
            if (backupDir.mkdirs()) {
                Log.d(TAG, "Created backup directory");
            } else {
                Log.e(TAG, "Failed to create backup directory");
            }
        }
        
        // Create templates directory
        this.templatesDir = new File(context.getFilesDir(), TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            if (templatesDir.mkdirs()) {
                Log.d(TAG, "Created templates directory");
                // Create default templates
                createDefaultTemplates();
            } else {
                Log.e(TAG, "Failed to create templates directory");
            }
        }
    }
    
    /**
     * Create a new project from a template
     * @param projectName The name of the project
     * @param templateName The name of the template to use
     * @return The project directory
     */
    public File createProject(String projectName, String templateName) {
        File projectDir = new File(projectsDir, projectName);
        
        // Check if project already exists
        if (projectDir.exists()) {
            Log.e(TAG, "Project already exists: " + projectName);
            return null;
        }
        
        // Create project directory
        if (!projectDir.mkdirs()) {
            Log.e(TAG, "Failed to create project directory: " + projectName);
            return null;
        }
        
        // Copy template to project directory if template name is provided
        if (templateName != null && !templateName.isEmpty()) {
            File templateDir = new File(templatesDir, templateName);
            if (templateDir.exists() && templateDir.isDirectory()) {
                try {
                    copyDirectory(templateDir, projectDir);
                    Log.d(TAG, "Created project from template: " + templateName);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to copy template: " + templateName, e);
                    projectDir.delete();
                    return null;
                }
            } else {
                Log.e(TAG, "Template doesn't exist: " + templateName);
            }
        }
        
        return projectDir;
    }
    
    /**
     * Delete a project
     * @param projectName The name of the project to delete
     * @return true if the project was deleted, false otherwise
     */
    public boolean deleteProject(String projectName) {
        File projectDir = new File(projectsDir, projectName);
        
        // Check if project exists
        if (!projectDir.exists()) {
            Log.e(TAG, "Project doesn't exist: " + projectName);
            return false;
        }
        
        // Delete project directory
        return deleteDirectory(projectDir);
    }
    
    /**
     * Create a backup of a project
     * @param projectName The name of the project to backup
     * @return The backup file
     */
    public File backupProject(String projectName) {
        File projectDir = new File(projectsDir, projectName);
        
        // Check if project exists
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            Log.e(TAG, "Project doesn't exist: " + projectName);
            return null;
        }
        
        // Create backup file
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String backupName = projectName + "_" + dateFormat.format(new Date()) + ".zip";
        File backupFile = new File(backupDir, backupName);
        
        try {
            // Create zip file
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupFile));
            zipDirectory(projectDir, projectDir.getName(), zipOut);
            zipOut.close();
            
            Log.d(TAG, "Created backup: " + backupName);
            return backupFile;
        } catch (IOException e) {
            Log.e(TAG, "Failed to create backup: " + backupName, e);
            return null;
        }
    }
    
    /**
     * Restore a project from a backup
     * @param backupFile The backup file
     * @param projectName The name to restore the project as (optional)
     * @return The restored project directory
     */
    public File restoreProject(File backupFile, String projectName) {
        // Check if backup file exists
        if (!backupFile.exists() || !backupFile.isFile()) {
            Log.e(TAG, "Backup file doesn't exist: " + backupFile.getName());
            return null;
        }
        
        // If project name is not provided, extract it from the backup filename
        if (projectName == null || projectName.isEmpty()) {
            String fileName = backupFile.getName();
            int underscoreIndex = fileName.indexOf('_');
            if (underscoreIndex > 0) {
                projectName = fileName.substring(0, underscoreIndex);
            } else {
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    projectName = fileName.substring(0, dotIndex);
                } else {
                    projectName = fileName;
                }
            }
        }
        
        // Create project directory
        File projectDir = new File(projectsDir, projectName);
        if (projectDir.exists()) {
            Log.e(TAG, "Project already exists: " + projectName);
            return null;
        }
        
        if (!projectDir.mkdirs()) {
            Log.e(TAG, "Failed to create project directory: " + projectName);
            return null;
        }
        
        try {
            // Unzip backup file to project directory
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(backupFile));
            ZipEntry entry = zipIn.getNextEntry();
            
            byte[] buffer = new byte[1024];
            
            while (entry != null) {
                String filePath = entry.getName();
                // Skip the root directory in the zip file
                if (filePath.indexOf('/') > 0) {
                    filePath = filePath.substring(filePath.indexOf('/') + 1);
                } else {
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                    continue;
                }
                
                File file = new File(projectDir, filePath);
                
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // Create parent directories if they don't exist
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    // Extract file
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    while ((len = zipIn.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            
            zipIn.close();
            
            Log.d(TAG, "Restored project: " + projectName);
            return projectDir;
        } catch (IOException e) {
            Log.e(TAG, "Failed to restore project: " + projectName, e);
            deleteDirectory(projectDir);
            return null;
        }
    }
    
    /**
     * Get the list of projects
     * @return A list of project names
     */
    public List<String> getProjects() {
        List<String> projects = new ArrayList<>();
        
        File[] files = projectsDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    projects.add(file.getName());
                }
            }
        }
        
        return projects;
    }
    
    /**
     * Get the list of available templates
     * @return A list of template names
     */
    public List<String> getTemplates() {
        List<String> templates = new ArrayList<>();
        
        File[] files = templatesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    templates.add(file.getName());
                }
            }
        }
        
        return templates;
    }
    
    /**
     * Get the list of backups
     * @return A list of backup files
     */
    public List<File> getBackups() {
        List<File> backups = new ArrayList<>();
        
        File[] files = backupDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".zip")) {
                    backups.add(file);
                }
            }
        }
        
        return backups;
    }
    
    /**
     * Get the project directory
     * @param projectName The name of the project
     * @return The project directory
     */
    public File getProjectDirectory(String projectName) {
        File projectDir = new File(projectsDir, projectName);
        
        if (projectDir.exists() && projectDir.isDirectory()) {
            return projectDir;
        } else {
            return null;
        }
    }
    
    /**
     * Create default templates
     */
    private void createDefaultTemplates() {
        // Create Android template
        createTemplate("android", createAndroidTemplate());
        
        // Create Flutter template
        createTemplate("flutter", createFlutterTemplate());
        
        // Create React Native template
        createTemplate("react-native", createReactNativeTemplate());
        
        // Create Web template (HTML/CSS/JS)
        createTemplate("web", createWebTemplate());
    }
    
    /**
     * Create a template from a list of files and content
     * @param templateName The name of the template
     * @param files A list of files and their content
     */
    private void createTemplate(String templateName, List<TemplateFile> files) {
        File templateDir = new File(templatesDir, templateName);
        
        if (!templateDir.exists() && !templateDir.mkdirs()) {
            Log.e(TAG, "Failed to create template directory: " + templateName);
            return;
        }
        
        for (TemplateFile file : files) {
            File outFile = new File(templateDir, file.path);
            
            // Create parent directories if they don't exist
            File parent = outFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            
            try {
                FileOutputStream fos = new FileOutputStream(outFile);
                fos.write(file.content.getBytes());
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create template file: " + file.path, e);
            }
        }
    }
    
    /**
     * Create a basic Android template
     * @return A list of template files
     */
    private List<TemplateFile> createAndroidTemplate() {
        List<TemplateFile> files = new ArrayList<>();
        
        // Add template files
        files.add(new TemplateFile("build.gradle", "// Top-level build file\n" +
                "buildscript {\n" +
                "    repositories {\n" +
                "        google()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "    dependencies {\n" +
                "        classpath 'com.android.tools.build:gradle:7.4.2'\n" +
                "    }\n" +
                "}\n\n" +
                "allprojects {\n" +
                "    repositories {\n" +
                "        google()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "}"));
        
        files.add(new TemplateFile("settings.gradle", "include ':app'"));
        
        files.add(new TemplateFile("app/build.gradle", "plugins {\n" +
                "    id 'com.android.application'\n" +
                "}\n\n" +
                "android {\n" +
                "    compileSdkVersion 33\n" +
                "    defaultConfig {\n" +
                "        applicationId \"com.example.app\"\n" +
                "        minSdkVersion 21\n" +
                "        targetSdkVersion 33\n" +
                "        versionCode 1\n" +
                "        versionName \"1.0\"\n" +
                "    }\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            minifyEnabled false\n" +
                "        }\n" +
                "    }\n" +
                "}\n\n" +
                "dependencies {\n" +
                "    implementation 'androidx.appcompat:appcompat:1.6.1'\n" +
                "    implementation 'com.google.android.material:material:1.9.0'\n" +
                "}"));
        
        files.add(new TemplateFile("app/src/main/AndroidManifest.xml", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"com.example.app\">\n\n" +
                "    <application\n" +
                "        android:allowBackup=\"true\"\n" +
                "        android:icon=\"@mipmap/ic_launcher\"\n" +
                "        android:label=\"@string/app_name\"\n" +
                "        android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
                "        android:supportsRtl=\"true\"\n" +
                "        android:theme=\"@style/Theme.App\">\n" +
                "        <activity\n" +
                "            android:name=\".MainActivity\"\n" +
                "            android:exported=\"true\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                "            </intent-filter>\n" +
                "        </activity>\n" +
                "    </application>\n" +
                "</manifest>"));
        
        files.add(new TemplateFile("app/src/main/java/com/example/app/MainActivity.java", 
                "package com.example.app;\n\n" +
                "import android.os.Bundle;\n" +
                "import androidx.appcompat.app.AppCompatActivity;\n\n" +
                "public class MainActivity extends AppCompatActivity {\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.activity_main);\n" +
                "    }\n" +
                "}"));
        
        files.add(new TemplateFile("app/src/main/res/layout/activity_main.xml", 
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<androidx.constraintlayout.widget.ConstraintLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    tools:context=\".MainActivity\">\n\n" +
                "    <TextView\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Hello World!\"\n" +
                "        app:layout_constraintBottom_toBottomOf=\"parent\"\n" +
                "        app:layout_constraintLeft_toLeftOf=\"parent\"\n" +
                "        app:layout_constraintRight_toRightOf=\"parent\"\n" +
                "        app:layout_constraintTop_toTopOf=\"parent\" />\n\n" +
                "</androidx.constraintlayout.widget.ConstraintLayout>"));
        
        return files;
    }
    
    /**
     * Create a basic Flutter template
     * @return A list of template files
     */
    private List<TemplateFile> createFlutterTemplate() {
        List<TemplateFile> files = new ArrayList<>();
        
        // Add template files
        files.add(new TemplateFile("pubspec.yaml", "name: flutter_app\n" +
                "description: A new Flutter project.\n" +
                "version: 1.0.0+1\n\n" +
                "environment:\n" +
                "  sdk: \">=2.12.0 <3.0.0\"\n\n" +
                "dependencies:\n" +
                "  flutter:\n" +
                "    sdk: flutter\n" +
                "  cupertino_icons: ^1.0.2\n\n" +
                "dev_dependencies:\n" +
                "  flutter_test:\n" +
                "    sdk: flutter\n\n" +
                "flutter:\n" +
                "  uses-material-design: true"));
        
        files.add(new TemplateFile("lib/main.dart", "import 'package:flutter/material.dart';\n\n" +
                "void main() {\n" +
                "  runApp(MyApp());\n" +
                "}\n\n" +
                "class MyApp extends StatelessWidget {\n" +
                "  @override\n" +
                "  Widget build(BuildContext context) {\n" +
                "    return MaterialApp(\n" +
                "      title: 'Flutter Demo',\n" +
                "      theme: ThemeData(\n" +
                "        primarySwatch: Colors.blue,\n" +
                "      ),\n" +
                "      home: MyHomePage(title: 'Flutter Demo Home Page'),\n" +
                "    );\n" +
                "  }\n" +
                "}\n\n" +
                "class MyHomePage extends StatefulWidget {\n" +
                "  MyHomePage({Key? key, required this.title}) : super(key: key);\n" +
                "  final String title;\n\n" +
                "  @override\n" +
                "  _MyHomePageState createState() => _MyHomePageState();\n" +
                "}\n\n" +
                "class _MyHomePageState extends State<MyHomePage> {\n" +
                "  int _counter = 0;\n\n" +
                "  void _incrementCounter() {\n" +
                "    setState(() {\n" +
                "      _counter++;\n" +
                "    });\n" +
                "  }\n\n" +
                "  @override\n" +
                "  Widget build(BuildContext context) {\n" +
                "    return Scaffold(\n" +
                "      appBar: AppBar(\n" +
                "        title: Text(widget.title),\n" +
                "      ),\n" +
                "      body: Center(\n" +
                "        child: Column(\n" +
                "          mainAxisAlignment: MainAxisAlignment.center,\n" +
                "          children: <Widget>[\n" +
                "            Text(\n" +
                "              'You have pushed the button this many times:',\n" +
                "            ),\n" +
                "            Text(\n" +
                "              '$_counter',\n" +
                "              style: Theme.of(context).textTheme.headline4,\n" +
                "            ),\n" +
                "          ],\n" +
                "        ),\n" +
                "      ),\n" +
                "      floatingActionButton: FloatingActionButton(\n" +
                "        onPressed: _incrementCounter,\n" +
                "        tooltip: 'Increment',\n" +
                "        child: Icon(Icons.add),\n" +
                "      ),\n" +
                "    );\n" +
                "  }\n" +
                "}"));
        
        return files;
    }
    
    /**
     * Create a basic React Native template
     * @return A list of template files
     */
    private List<TemplateFile> createReactNativeTemplate() {
        List<TemplateFile> files = new ArrayList<>();
        
        // Add template files
        files.add(new TemplateFile("package.json", "{\n" +
                "  \"name\": \"reactnativeapp\",\n" +
                "  \"version\": \"0.0.1\",\n" +
                "  \"private\": true,\n" +
                "  \"scripts\": {\n" +
                "    \"android\": \"react-native run-android\",\n" +
                "    \"ios\": \"react-native run-ios\",\n" +
                "    \"start\": \"react-native start\"\n" +
                "  },\n" +
                "  \"dependencies\": {\n" +
                "    \"react\": \"18.2.0\",\n" +
                "    \"react-native\": \"0.72.4\"\n" +
                "  },\n" +
                "  \"devDependencies\": {\n" +
                "    \"@babel/core\": \"^7.20.0\",\n" +
                "    \"@babel/preset-env\": \"^7.20.0\",\n" +
                "    \"@babel/runtime\": \"^7.20.0\"\n" +
                "  }\n" +
                "}"));
        
        files.add(new TemplateFile("index.js", "import {AppRegistry} from 'react-native';\n" +
                "import App from './App';\n" +
                "import {name as appName} from './app.json';\n\n" +
                "AppRegistry.registerComponent(appName, () => App);"));
        
        files.add(new TemplateFile("app.json", "{\n" +
                "  \"name\": \"reactnativeapp\",\n" +
                "  \"displayName\": \"React Native App\"\n" +
                "}"));
        
        files.add(new TemplateFile("App.js", "import React from 'react';\n" +
                "import {SafeAreaView, StatusBar, StyleSheet, Text, View} from 'react-native';\n\n" +
                "const App = () => {\n" +
                "  return (\n" +
                "    <SafeAreaView style={styles.container}>\n" +
                "      <StatusBar barStyle=\"dark-content\" />\n" +
                "      <View style={styles.content}>\n" +
                "        <Text style={styles.title}>Welcome to React Native</Text>\n" +
                "        <Text style={styles.text}>\n" +
                "          This is a simple React Native application template.\n" +
                "        </Text>\n" +
                "      </View>\n" +
                "    </SafeAreaView>\n" +
                "  );\n" +
                "};\n\n" +
                "const styles = StyleSheet.create({\n" +
                "  container: {\n" +
                "    flex: 1,\n" +
                "    backgroundColor: '#F5FCFF',\n" +
                "  },\n" +
                "  content: {\n" +
                "    flex: 1,\n" +
                "    justifyContent: 'center',\n" +
                "    alignItems: 'center',\n" +
                "    padding: 20,\n" +
                "  },\n" +
                "  title: {\n" +
                "    fontSize: 24,\n" +
                "    fontWeight: 'bold',\n" +
                "    marginBottom: 20,\n" +
                "  },\n" +
                "  text: {\n" +
                "    fontSize: 16,\n" +
                "    textAlign: 'center',\n" +
                "  },\n" +
                "});\n\n" +
                "export default App;"));
        
        return files;
    }
    
    /**
     * Create a basic Web template
     * @return A list of template files
     */
    private List<TemplateFile> createWebTemplate() {
        List<TemplateFile> files = new ArrayList<>();
        
        // Add template files
        files.add(new TemplateFile("index.html", "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Web App</title>\n" +
                "    <link rel=\"stylesheet\" href=\"styles.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Welcome to Web App</h1>\n" +
                "        <p>This is a basic HTML/CSS/JS template.</p>\n" +
                "        <button id=\"counter-btn\">Click Me</button>\n" +
                "        <p id=\"counter\">Clicks: 0</p>\n" +
                "    </div>\n" +
                "    <script src=\"script.js\"></script>\n" +
                "</body>\n" +
                "</html>"));
        
        files.add(new TemplateFile("styles.css", "* {\n" +
                "    box-sizing: border-box;\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "}\n\n" +
                "body {\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    line-height: 1.6;\n" +
                "    background-color: #f4f4f4;\n" +
                "    color: #333;\n" +
                "}\n\n" +
                ".container {\n" +
                "    max-width: 800px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 30px;\n" +
                "    text-align: center;\n" +
                "}\n\n" +
                "h1 {\n" +
                "    margin-bottom: 20px;\n" +
                "    color: #444;\n" +
                "}\n\n" +
                "p {\n" +
                "    margin-bottom: 20px;\n" +
                "}\n\n" +
                "button {\n" +
                "    padding: 10px 20px;\n" +
                "    background-color: #4CAF50;\n" +
                "    color: white;\n" +
                "    border: none;\n" +
                "    border-radius: 4px;\n" +
                "    cursor: pointer;\n" +
                "    margin-bottom: 20px;\n" +
                "}\n\n" +
                "button:hover {\n" +
                "    background-color: #45a049;\n" +
                "}"));
        
        files.add(new TemplateFile("script.js", "// Simple counter script\n" +
                "let counter = 0;\n" +
                "const counterBtn = document.getElementById('counter-btn');\n" +
                "const counterEl = document.getElementById('counter');\n\n" +
                "counterBtn.addEventListener('click', () => {\n" +
                "    counter++;\n" +
                "    counterEl.textContent = `Clicks: ${counter}`;\n" +
                "});\n\n" +
                "// Display a greeting based on time of day\n" +
                "function getGreeting() {\n" +
                "    const hour = new Date().getHours();\n" +
                "    let greeting;\n\n" +
                "    if (hour < 12) {\n" +
                "        greeting = 'Good morning';\n" +
                "    } else if (hour < 18) {\n" +
                "        greeting = 'Good afternoon';\n" +
                "    } else {\n" +
                "        greeting = 'Good evening';\n" +
                "    }\n\n" +
                "    console.log(`${greeting}! Welcome to the Web App.`);\n" +
                "}\n\n" +
                "// Call the greeting function when the page loads\n" +
                "getGreeting();"));
        
        return files;
    }
    
    /**
     * Helper method to copy a directory
     * @param src The source directory
     * @param dst The destination directory
     */
    private void copyDirectory(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            if (!dst.exists()) {
                dst.mkdirs();
            }
            
            String[] files = src.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(src, file);
                    File dstFile = new File(dst, file);
                    
                    copyDirectory(srcFile, dstFile);
                }
            }
        } else {
            // Copy file
            try (FileInputStream in = new FileInputStream(src);
                 FileOutputStream out = new FileOutputStream(dst)) {
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }
    
    /**
     * Helper method to delete a directory
     * @param directory The directory to delete
     * @return true if the directory was deleted, false otherwise
     */
    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }
    
    /**
     * Helper method to zip a directory
     * @param folder The directory to zip
     * @param parentFolder The parent folder name
     * @param zipOut The zip output stream
     */
    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        File[] files = folder.listFiles();
        byte[] buffer = new byte[1024];
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, parentFolder + "/" + file.getName(), zipOut);
                    continue;
                }
                
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                zipOut.putNextEntry(zipEntry);
                
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                
                fis.close();
                zipOut.closeEntry();
            }
        }
    }
    
    /**
     * Helper class for template files
     */
    private static class TemplateFile {
        public String path;
        public String content;
        
        public TemplateFile(String path, String content) {
            this.path = path;
            this.content = content;
        }
    }
}