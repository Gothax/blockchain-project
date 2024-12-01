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
        try {
            boolean validate = operator.validate();
            System.out.println("result = " + validate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // then
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
        String unlockingScript = "1";
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


    @Test
    @DisplayName("tx1 적절한 sig, key 생성")
    void makeTxExample1() {
        // given

//        tx:
//        input: tx1, 0, 100, OP_DUP OP_HASH <public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT, <signature> <public key>
//        output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//        output, 1: 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT

        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "tx1";
        String signature = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String pubKey = java.util.Base64.getEncoder().encodeToString(encoded);
        String hashedPubKey = SHA256.encryptGetEncode(pubKey);

        System.out.println("========넣어줘야 하는 값 시작, previous tx id hash가 tx1이라고 가정========");
        System.out.println("<public key> = " + pubKey);
        System.out.println("<public key hash> = " + hashedPubKey);
        System.out.println("<signature> = " + signature);
        System.out.println("========넣어줘야 하는 값 끝========");
        String lockingScript = "OP_DUP OP_HASH " + SHA256.encryptGetEncode(pubKey) + " OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT";
        String unlockingScript = signature + " " + pubKey;
        Operator operator = new Operator(lockingScript, unlockingScript, message);
        // when
        boolean result = operator.validate();

        // then
        System.out.println("result = " + result);
        assertTrue(result);
    }

    @Test
    @DisplayName("tx2 적절한 sig, key 생성")
    void makeTx2Example() {
        // given
//        tx:
//        input: tx2, 0, 100, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT, <signature1> <signature2>
//        output, 0: 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//        output, 1: 60, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT

        // when
        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "tx2";
        String signature1 = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String pubKey1 = java.util.Base64.getEncoder().encodeToString(encoded);

        KeyPair keyPair2 = ECDSA.generateRandomKeyPair();
        String signature2 = ECDSA.sign(message, keyPair2.getPrivate());
        byte[] encoded2 = keyPair2.getPublic().getEncoded();
        String pubKey2 = java.util.Base64.getEncoder().encodeToString(encoded2);

        KeyPair keyPair3 = ECDSA.generateRandomKeyPair();
        byte[] encoded3 = keyPair3.getPublic().getEncoded();
        String pubKey3 = java.util.Base64.getEncoder().encodeToString(encoded3);

        System.out.println("========넣어줘야 하는 값 시작, previous tx id hash가 tx2이라고 가정========");
        System.out.println("<pubKey1> = " + pubKey1);
        System.out.println("<pubKey2> = " + pubKey2);
        System.out.println("<pubKey3> = " + pubKey3);

        System.out.println("<signature1> = " + signature1);
        System.out.println("<signature2> = " + signature2);
        System.out.println("========넣어줘야 하는 값 끝========");

        // then
        String lockingScript = "2 " + pubKey1 + " " + pubKey2 + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT";
        String unlockingScript = signature1 + " " + signature2;
        Operator operator = new Operator(lockingScript, unlockingScript, message);
        boolean result = operator.validate();

        System.out.println("result = " + result);
        assertTrue(result);
    }

    @Test
    @DisplayName("tx3 적절한 sig, key 생성")
    void makeTx3Example() {
        // given
//        tx:
//        input: tx3, 0, 100, OP_DUP OP_HASH <script X hash> OP_EQUALVERIFY, <script X>
//        output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//        output, 1: 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//
//        script X = <Alice signature> <Alice public key> 1 OP_IF OP_DUP OP_HASH <public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 <Alice pubKey> <Bob pubKey> <pubKey3> 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT
//  alice sig, pubkey, bob pubkey, pubkey3 (pubkey 3개, valid한 서명 1개), scriptX hash(unlocking script)
        // when
        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "tx3";
        String aliceSig = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String alicePubKey = java.util.Base64.getEncoder().encodeToString(encoded);
        String aliceHashedPubKey = SHA256.encryptGetEncode(alicePubKey);

        KeyPair keyPair2 = ECDSA.generateRandomKeyPair();
        byte[] encoded2 = keyPair2.getPublic().getEncoded();
        String bobPubKey = java.util.Base64.getEncoder().encodeToString(encoded2);

        KeyPair keyPair3 = ECDSA.generateRandomKeyPair();
        byte[] encoded3 = keyPair3.getPublic().getEncoded();
        String pubKey3 = java.util.Base64.getEncoder().encodeToString(encoded3);

//        String scriptXHash = SHA256.encryptGetEncode(aliceSig + " " + alicePubKey + " 1 OP_IF OP_DUP OP_HASH " + aliceHashedPubKey + " OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 " + alicePubKey + " " + bobPubKey + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT");



        // then
        String scriptX = "OP_IF OP_DUP OP_HASH " + aliceHashedPubKey + " OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 " + alicePubKey + " " + bobPubKey + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT";
        String unlockingScript = aliceSig + " " + alicePubKey + " 1 " + scriptX;
//        String unlockingScript = aliceSig + " " + alicePubKey + " 1 OP_IF OP_DUP OP_HASH " + aliceHashedPubKey + " OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 " + alicePubKey + " " + bobPubKey + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT";
        String scriptXHash = SHA256.encryptGetEncode(scriptX);
        String lockingScript = "OP_DUP OP_HASH " + scriptXHash + " OP_EQUALVERIFY";
        Operator operator = new Operator(lockingScript, unlockingScript, message);
        boolean result = operator.validate();



        System.out.println("========넣어줘야 하는 값 시작, previous tx id hash가 tx3이라고 가정========");
        System.out.println("<Alice public key> = " + alicePubKey);
        System.out.println("<Alice public key hash> = " + aliceHashedPubKey);
        System.out.println("<Alice signature> = " + aliceSig);
        System.out.println("<Bob public key> = " + bobPubKey);
        System.out.println("<pubKey3> = " + pubKey3);
        System.out.println("scriptXHash = " + scriptXHash);
        System.out.println("unlockingScript = " + unlockingScript);
        System.out.println("lockingScript = " + lockingScript);
        System.out.println(lockingScript + ", " + unlockingScript);
        System.out.println("========넣어줘야 하는 값 끝========");


        System.out.println("result = " + result);
        assertTrue(result);
    }

    @Test
    @DisplayName("tx4 적절한 sig, key 생성")
    void makeTx4Example() {
        // given
//        tx:
//        input: tx4, 0, 100, OP_DUP OP_HASH <script X hash> OP_EQUALVERIFY, <script X>
//        output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//        output, 1: 10, OP_DUP OP_HASH <Alice public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//        output, 2: 30, OP_DUP OP_HASH <Bob public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
//
//        script X = <Alice signature> <Bob signature> 0 OP_IF OP_DUP OP_HASH <public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 <Alice pubKey> <Bob pubKey> <pubKey3> 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT
        // alic sig, bob sig, alice pubkey, bob pubkey, pubkey3 (pubkey 3개, valid한 서명 2개)
        // when

        KeyPair keyPair = ECDSA.generateRandomKeyPair();
        String message = "tx4";
        String aliceSig = ECDSA.sign(message, keyPair.getPrivate());
        byte[] encoded = keyPair.getPublic().getEncoded();
        String alicePubKey = java.util.Base64.getEncoder().encodeToString(encoded);
        String aliceHashedPubKey = SHA256.encryptGetEncode(alicePubKey);

        KeyPair keyPair2 = ECDSA.generateRandomKeyPair();
        String bobSig = ECDSA.sign(message, keyPair2.getPrivate());
        byte[] encoded2 = keyPair2.getPublic().getEncoded();
        String bobPubKey = java.util.Base64.getEncoder().encodeToString(encoded2);

        KeyPair keyPair3 = ECDSA.generateRandomKeyPair();
        byte[] encoded3 = keyPair3.getPublic().getEncoded();
        String pubKey3 = java.util.Base64.getEncoder().encodeToString(encoded3);



        // then
//        String unlockingScript = aliceSig + " " + bobSig + " 0 OP_IF OP_DUP OP_HASH " + aliceHashedPubKey + " OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 " + alicePubKey + " " + bobPubKey + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT";
        String scriptX = "OP_IF OP_DUP OP_HASH " + aliceHashedPubKey + " OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 " + alicePubKey + " " + bobPubKey + " " + pubKey3 + " 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT";
        String unlockingScript = aliceSig + " " + bobSig + " 0 " + scriptX;
        String scriptXHash = SHA256.encryptGetEncode(scriptX);
        String lockingScript = "OP_DUP OP_HASH " + scriptXHash + " OP_EQUALVERIFY";


        System.out.println("========넣어줘야 하는 값 시작, previous tx id hash가 tx4이라고 가정========");
        System.out.println("<Alice public key> = " + alicePubKey);
        System.out.println("<Alice public key hash> = " + aliceHashedPubKey);
        System.out.println("<Alice signature> = " + aliceSig);
        System.out.println("<Bob public key> = " + bobPubKey);
        System.out.println("<Bob signature> = " + bobSig);
        System.out.println("<pubKey3> = " + pubKey3);
        System.out.println("scriptXHash = " + scriptXHash);
        System.out.println("unlockingScript = " + unlockingScript);
        System.out.println("lockingScript = " + lockingScript);
        System.out.println(lockingScript + ", " + unlockingScript);
        System.out.println("========넣어줘야 하는 값 끝========");

        Operator operator = new Operator(lockingScript, unlockingScript, message);
        boolean result = operator.validate();

        System.out.println("result = " + result);
        assertTrue(result);
    }


}