package com.gothaxcity.service;

import com.gothaxcity.util.ECDSA;
import com.gothaxcity.util.SHA256;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

public class Operator {

    private final ArrayDeque<Object> stack = new ArrayDeque<>();
    private final String lockingScript;
    private final String unlockingScript;
    private final String message;

    public Operator(String lockingScript, String unlockingScript, String message) {
        this.lockingScript = lockingScript;
        this.unlockingScript = unlockingScript;
        this.message = message;
    }

    public boolean validate() {

        // unlockingScript를 stack에 넣는다
        stream(unlockingScript.split(" "))
                .toList()
                .forEach(stack::push);
        System.out.println("stack = " + stack);

        // lockingScript를 실행
        try {
            stream(lockingScript.split(" ")).forEach(this::execute);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return stack.pop().equals(true) && stack.isEmpty();
    }

    private void execute(String s) {
        System.out.println("stack = " + stack + "script = " + s);
        if (s.equals("DUP")) {
            stack.push(stack.peek());
            return;
        }
        if (s.equals("HASH")) {
            hash();
            return;
        }
        if (s.equals("EQUAL")) {
            stack.push(equals());
            return;
        }
        if (s.equals("EQUALVERIFY")) {
            if (!equals()) {
                throw new IllegalArgumentException("검증 실패");
            }
            return;
        }
        if (s.equals("CHECKSIG")) {
            boolean result = checkSignature();
            stack.push(result);
            return;
        }
        if (s.equals("CHECKSIGVERIFY")) {
            if (!checkSignature()) {
                throw new IllegalArgumentException("검증 실패");
            }
            return;
        }
        if (s.equals("CHECKMULTISIG")) {
            boolean result = checkMultiSignature();
            stack.push(result);
            return;
        }

        stack.push(s);
    }

    private boolean checkMultiSignature() {
        List<String> pubKeys = popAndGetN();
        List<String> sigs = popAndGetN();

        int validSignatures = getValidSignatures(sigs, pubKeys);

        return validSignatures == sigs.size();
    }

    private int getValidSignatures(List<String> sigs, List<String> pubKeys) {
        int validSignatures = 0;
        for (String sig : sigs) {
            boolean isValid = false;
            for (String pubKey : pubKeys) {
                if (ECDSA.verifySigWithPubKey(message, sig, pubKey)) {
                    isValid = true;
                    break; // 서명이 유효하면 다른 키로는 검증 안함
                }
            }
            if (isValid) {
                validSignatures++;
                continue;
            }
            return 0; // 하나라도 유효하지 않은 서명이 있으면 전체 검증 실패
        }
        return validSignatures;
    }

    private List<String> popAndGetN() {
        int n = (int) stack.pop();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            keys.add((String) stack.pop());
        }
        return keys;
    }

    private boolean checkSignature() {
        Object pubKey = stack.pop();
        Object sig = stack.pop();
        return ECDSA.verifySigWithPubKey(message, (String) sig, (String) pubKey);
    }

    private boolean equals() {
        return stack.pop().equals(stack.pop());
    }

    private void hash() {
        String pop = (String) stack.pop();
        stack.push(SHA256.encryptGetEncode(pop));
    }
}
