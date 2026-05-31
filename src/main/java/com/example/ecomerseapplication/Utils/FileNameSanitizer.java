package com.example.ecomerseapplication.Utils;

public class FileNameSanitizer {

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) return null;

        fileName = fileName.replace("\\", "/");
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
