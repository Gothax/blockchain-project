package com.gothaxcity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.Base64.*;

public class SHA256 {

    public static String encryptGetEncode(String input) {
        try {
            byte[] digest = encrypt(input);
            return getBase64(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptGetBytes(String input) {
        try {
            return encrypt(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] encrypt(String input) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        instance.update(input.getBytes());
        return instance.digest();
    }

    private static String getHex(byte[] digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static String getBase64(byte[] digest) {
        return getEncoder().encodeToString(digest);
    }
}

