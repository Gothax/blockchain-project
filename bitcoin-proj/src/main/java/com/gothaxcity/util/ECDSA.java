package com.gothaxcity.util;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDSA {


    public static boolean verifySigWithPubKey(String message, String sig, String publicKey) {
        try {
            byte[] messageHash = SHA256.encryptGetBytes(message);
            byte[] sigBytes = Base64.getDecoder().decode(sig);
            PublicKey pubKey = getPublicKeyFromString(publicKey);

            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(pubKey);
            signature.update(messageHash);
            return signature.verify(sigBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String sign(String message, PrivateKey privateKey) {
        try {
            byte[] messageHash = SHA256.encryptGetBytes(message);

            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(messageHash);
            return Base64.getEncoder().encodeToString(ecdsaSign.sign());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static KeyPair generateRandomKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");

            // jdk21에서 secp256k1 지원하지 않아서 secp256r1 사용
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            generator.initialize(ecSpec, new SecureRandom());
            return generator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PublicKey getPublicKeyFromString(String publicKey) {

        try {
            byte[] pubKeyByte = Base64.getDecoder().decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(new X509EncodedKeySpec(pubKeyByte));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
