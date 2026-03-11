package com.example.appsentinel.security;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashGenerator {

    public static String generateSHA256(String path) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            FileInputStream fis = new FileInputStream(path);

            byte[] buffer = new byte[1024];
            int read;

            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hash = digest.digest();

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            return "";
        }

    }

}