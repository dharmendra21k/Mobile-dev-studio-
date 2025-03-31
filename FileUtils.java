package com.mobiledev.androidstudio.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file operations
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * Read a file to a string
     *
     * @param file File to read
     * @return File contents
     */
    public static String readFile(File file) {
        try {
            StringBuilder fileContents = new StringBuilder();
            Scanner scanner = new Scanner(file);
            
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append("\n");
            }
            
            scanner.close();
            return fileContents.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error reading file", e);
            return "";
        }
    }

    /**
     * Write a string to a file
     *
     * @param file File to write
     * @param content Content to write
     * @return True if successful
     */
    public static boolean writeFile(File file, String content) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing file", e);
            return false;
        }
    }

    /**
     * Copy a file
     *
     * @param sourcePath Source path
     * @param destPath Destination path
     * @return True if successful
     */
    public static boolean copyFile(String sourcePath, String destPath) {
        try (InputStream in = new FileInputStream(sourcePath);
             OutputStream out = new FileOutputStream(destPath)) {
            
            byte[] buffer = new byte[1024];
            int length;
            
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return false;
        }
    }

    /**
     * Delete a file or directory
     *
     * @param file File or directory to delete
     * @return True if successful
     */
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        
        return file.delete();
    }

    /**
     * Create a zip file from a directory
     *
     * @param sourceDir Source directory
     * @param outputPath Output path
     * @return Path to the created zip file, or null if failed
     */
    public static String createZipFile(String sourceDir, String outputPath) {
        try {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputPath));
            File fileToZip = new File(sourceDir);
            
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            
            return outputPath;
        } catch (IOException e) {
            Log.e(TAG, "Error creating zip file", e);
            return null;
        }
    }

    /**
     * Zip a file or directory
     *
     * @param fileToZip File to zip
     * @param fileName File name
     * @param zipOut Zip output stream
     * @throws IOException If an I/O error occurs
     */
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            
            return;
        }
        
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        
        fis.close();
    }

    /**
     * Find the first file with a specific extension in a directory
     *
     * @param directory Directory to search
     * @param extension File extension
     * @return First file found, or null if none
     */
    public static File findFirstFileWithExtension(File directory, String extension) {
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith("." + extension)) {
                return file;
            } else if (file.isDirectory()) {
                File result = findFirstFileWithExtension(file, extension);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    /**
     * Check if a file contains a specific string
     *
     * @param file File to check
     * @param searchString String to search for
     * @return True if found
     */
    public static boolean findInFile(File file, String searchString) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchString)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error searching in file", e);
        }
        
        return false;
    }

    /**
     * Get all files in a directory
     *
     * @param directory Directory to search
     * @param recursive True to search recursively
     * @return List of files
     */
    public static List<File> getFiles(File directory, boolean recursive) {
        List<File> files = new ArrayList<>();
        
        if (!directory.exists() || !directory.isDirectory()) {
            return files;
        }
        
        File[] filesList = directory.listFiles();
        if (filesList == null) {
            return files;
        }
        
        for (File file : filesList) {
            if (file.isFile()) {
                files.add(file);
            } else if (recursive && file.isDirectory()) {
                files.addAll(getFiles(file, true));
            }
        }
        
        return files;
    }

    /**
     * Get file extension
     *
     * @param fileName File name
     * @return File extension
     */
    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Get the MIME type of a file
     *
     * @param fileName File name
     * @return MIME type
     */
    public static String getMimeType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "html":
            case "htm":
                return "text/html";
            case "js":
                return "application/javascript";
            case "css":
                return "text/css";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "zip":
                return "application/zip";
            case "java":
                return "text/x-java";
            case "kt":
                return "text/x-kotlin";
            case "c":
                return "text/x-c";
            case "cpp":
                return "text/x-c++";
            case "py":
                return "text/x-python";
            default:
                return "application/octet-stream";
        }
    }
}