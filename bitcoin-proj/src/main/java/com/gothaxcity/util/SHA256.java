package com.gothaxcity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {

    public static String encrypt(String input) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(input.getBytes());
            byte[] digest = instance.digest();

            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

