package com.gothaxcity.service;

import com.gothaxcity.util.ECDSA;
import com.gothaxcity.util.SHA256;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorTest {

    @Test
    @DisplayName("script를 받아서 스택에 넣는다")
    void pushScriptToStack() {
        // given

        String lockingScript = "OP_DUP OP_HASH <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT";
        String unlockingScript = "<sig> <pubKey>";
        Operator operator = new Operator(lockingScript, unlockingScript, "message");
        // when
        boolean validate = operator.validate();
        // then
        System.out.println("result = " + validate);
    }


    @Test
    @DisplayName("P2PKH SCRIPT test 성공 사례 - 테스트 메시지")
    void testP2PKHWithTestMessage() {
        // given
        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "Hello this is test message";
        String signature = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(encoded);


//        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
//        String unlockingScript = "<sig> <pubKey>";
        String lockingScript = "OP_DUP OP_HASH " + SHA256.encryptGetEncode(publicKeyBase64) + " OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT";
        String unlockingScript = signature + " " + publicKeyBase64;
        Operator operator = new Operator(lockingScript, unlockingScript, message);
        // when
        boolean result = operator.validate();

        // then
        System.out.println("result = " + result);
        assertTrue(result);
    }

    @Test
    @DisplayName("IF ELSE ENDIF 흐름 테스트")
    void ifEndifTest() {
        // given
        String unlockingScript = "true";
        String lockingScript = "OP_IF 5 OP_ELSE 10 OP_ENDIF OP_CHECKFINALRESULT";
        // when
        Operator operator = new Operator(lockingScript, unlockingScript, "message");
        boolean validate = operator.validate();
        System.out.println("validate = " + validate);
        // then
    }

    @Test
    @DisplayName("IF ELSE ENDIF 흐름 테스트 복잡한 버전")
    void ifEndifTest2() {
        // given
        String unlockingScript = "true";
        String lockingScript = "OP_IF 5 <sig> <sig2> <sig3> OP_ELSE 10 <pub> <pub2> OP_ENDIF OP_CHECKFINALRESULT";
        // when
        Operator operator = new Operator(lockingScript, unlockingScript, "message");
        boolean validate = operator.validate();
        System.out.println("validate = " + validate);
        // then
    }


    @Test
    @DisplayName("MULTISIG SCRIPT test 성공 사례 - M개의 유효 서명")
    void testMultiSigWithValidSignatures() {
        // given
        int n = 3; // 총 공개 키 개수
        int m = 2; // 필요한 서명 개수

        String message = "Test multisig message";

        // N개의 키 쌍 생성 (공개 키, 개인 키)
        List<KeyPair> keyPairs = new ArrayList<>();

        List<String> publicKeys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            KeyPair keyPair = ECDSA.generateRandomKeyPair();
            keyPairs.add(keyPair);
            String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            publicKeys.add(publicKeyBase64);
        }

        // M개의 서명 생성
        List<String> signatures = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            KeyPair keyPair = keyPairs.get(i); // 첫 M개의 키를 사용하여 서명 생성
            String signature = ECDSA.sign(message, keyPair.getPrivate());
            signatures.add(signature);
        }

        // LOCKING SCRIPT 구성
        // "M <pubKey1> <pubKey2> ... <pubKeyN> N OP_CHECKMULTISIG OP_CHECKFINALRESULT"
        String lockingScript = m + " " + String.join(" ", publicKeys) + " " + n + " OP_CHECKMULTISIG OP_CHECKFINALRESULT";

        // UNLOCKING SCRIPT 구성
        // "<sig1> <sig2> ..."
        String unlockingScript = String.join(" ", signatures);

        Operator operator = new Operator(lockingScript, unlockingScript, message);

        // when
        boolean result = operator.validate();

        // then
        System.out.println("result = " + result);
        assertTrue(result);
    }


    @Test
    @DisplayName("P2SH SCRIPT test 간단한 성공 사례")
    void testP2SH() {
        // given
        String message = "P2SH test message";

        // Inner script (subScriptX): 실행될 스크립트
        String subScriptX = "5 5 OP_EQUAL OP_CHECKFINALRESULT";

        // SubScriptX 해시값
        String scriptXHash = SHA256.encryptGetEncode(subScriptX);

        // Locking Script: DUP HASH <scriptXHash> EQUALVERIFY
        String lockingScript = "OP_DUP OP_HASH " + scriptXHash + " OP_EQUALVERIFY";

        // Unlocking Script: <scriptX>
        String unlockingScript = subScriptX;

        // Operator 생성
        Operator operator = new Operator(lockingScript, unlockingScript, message);

        // when
        boolean result = operator.validate();

        // then
        System.out.println("result = " + result);
        assertTrue(result);
    }




}