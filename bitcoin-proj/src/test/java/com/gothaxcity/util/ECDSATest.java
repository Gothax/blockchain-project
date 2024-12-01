package com.gothaxcity.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ECDSATest {


    @Test
    @DisplayName("서명 검증 성공 case")
    void validationSuccess() {
        // given
        KeyPair keyPair = ECDSA.generateRandomKeyPair();

        PrivateKey aPrivate = keyPair.getPrivate();

        PublicKey aPublic = keyPair.getPublic();
        byte[] encoded = aPublic.getEncoded();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(encoded);

        // 이 메시지가 transaction 전체
        String message = "Hello this is test message";
        String signature = ECDSA.sign(message, keyPair.getPrivate());
        // when
        boolean result = ECDSA.verifySigWithPubKey(message, signature, publicKeyBase64);
        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("서명 검증 fail case")
    void validationFail() {
        // given
        KeyPair keyPair = ECDSA.generateRandomKeyPair();

        PrivateKey aPrivate = keyPair.getPrivate();

        PublicKey aPublic = keyPair.getPublic();
        byte[] encoded = aPublic.getEncoded();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(encoded);

        // 이 메시지가 transaction 전체
        String message = "Hello this is test message";
        String signature = ECDSA.sign(message, keyPair.getPrivate());

        // when
        String wrongEncodedSig = Base64.getEncoder().encodeToString("wrongSignature".getBytes());
        boolean result = ECDSA.verifySigWithPubKey(message, wrongEncodedSig, publicKeyBase64);

        // then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            assertFalse(result);
        });
    }
}