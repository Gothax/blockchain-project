package com.gothaxcity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Function {

    public static byte[] hash(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes());
            return digest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

