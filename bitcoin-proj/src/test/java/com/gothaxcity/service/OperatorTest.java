package com.gothaxcity.service;

import com.gothaxcity.util.ECDSA;
import com.gothaxcity.util.SHA256;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

class OperatorTest {

    @Test
    @DisplayName("script를 받아서 스택에 넣는다")
    void pushScriptToStack() {
        // given

        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
        String unlockingScript = "<sig> <pubKey>";
        Operator operator = new Operator(lockingScript, unlockingScript, "message");
        // when
        boolean validate = operator.validate();
        // then
        System.out.println("result = " + validate);
    }


    @Test
    @DisplayName("P2PKH SCRIPT test 성공 사례")
    void testP2PKH() {
        // given
        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "Hello this is test message";
        String signature = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(encoded);


//        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
//        String unlockingScript = "<sig> <pubKey>";
        String lockingScript = "DUP HASH " + SHA256.encryptGetEncode(publicKeyBase64) + " EQUALVERIFY CHECKSIG";
        String unlockingScript = signature + " " + publicKeyBase64;
        Operator operator = new Operator(lockingScript, unlockingScript, message);
        // when
        boolean result = operator.validate();

        // then
        System.out.println("result = " + result);
    }



}