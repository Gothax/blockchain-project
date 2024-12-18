package com.gothaxcity.service;

import com.gothaxcity.util.ECDSA;
import com.gothaxcity.util.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Operator {

    private static final Logger logger = LoggerFactory.getLogger(Operator.class);

    private final ArrayDeque<Object> stack;
    private final String lockingScript;
    private final String unlockingScript;
    private final String message;

    public Operator(String lockingScript, String unlockingScript, String message) {
        this.lockingScript = lockingScript.strip();
        this.unlockingScript = unlockingScript.strip();
        this.message = message.strip();
        stack = new ArrayDeque<>();
    }

    public boolean validate(){

        // P2SH 방식인 경우 locking script에 OP_CHECKFINALRESULT 없음
        if (!lockingScript.contains("OP_CHECKFINALRESULT")) {
            String scriptX = getScriptXFromUnlockingScript();
            stack.push(scriptX);
            boolean subResult = start(lockingScript);
            if (!subResult) {
                return false;
            }
            stack.clear();
            return start(unlockingScript);
        }
        // P2PKH, MULTISIGNATURE 방식인 경우
        return start(unlockingScript + " " + lockingScript);
    }

    private boolean start(String entireScript) {

        boolean skip = false;
        for (String script : entireScript.split(" ")) {
            if (script.equals("OP_CHECKFINALRESULT")) {
                return stack.pop().equals(true) && stack.isEmpty();
            }
            if (script.equals("OP_IF")) {
                Object popped = stack.pop();
                if (popped.equals("true") | popped.equals("1")) {
                    skip = false;
                    continue;
                }
                skip = true;
                continue;
            }
            if (script.equals("OP_ELSE")) {
                skip = !skip;
                continue;
            }
            if (script.equals("OP_ENDIF")) {
                skip = false;
                continue;
            }
            if (!skip) {
                execute(script);
                logger.debug("현 script = {} 실행 후 stack {}", script, stack);
            }
        }
        // P2SH script hash 검증이 성공 (OP_CHECKFINALRESULT로 분기하지 않는 경우는 이것밖에 없음)
        // P2SH script hash가 달랐다면 예외 발생
        return true;
    }

    // OP 명령어중 하나면 실행, 아니면 스택에 push
    private void execute(String s) {
        logger.debug("현 script = {} 실행 전 stack {}", s, stack);
        if (s.equals("OP_DUP")) {
            stack.push(stack.peek());
            return;
        }
        if (s.equals("OP_HASH")) {
            hash();
            return;
        }
        if (s.equals("OP_EQUAL")) {
            stack.push(equals());
            return;
        }
        if (s.equals("OP_EQUALVERIFY")) {
            if (!equals()) {
                throw new IllegalArgumentException("OP_EQUALVERIFY 검증 실패");
            }
            return;
        }
        if (s.equals("OP_CHECKSIG")) {
            boolean result = checkSignature();
            stack.push(result);
            return;
        }
        if (s.equals("OP_CHECKSIGVERIFY")) {
            if (!checkSignature()) {
                throw new IllegalArgumentException("OP_CHECKSIGVERIFY 검증 실패");
            }
            return;
        }
        if (s.equals("OP_CHECKMULTISIG")) {
            boolean result = checkMultiSignature();
            stack.push(result);
            return;
        }
        if (s.equals("OP_CHECKMULTISIGVERIFY")) {
            if (!checkMultiSignature()) {
                throw new IllegalArgumentException("OP_CHECKMULTISIGVERIFY 검증 실패");
            }
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
        int n = Integer.parseInt(stack.pop().toString());
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

    private String getScriptXFromUnlockingScript() {
        int i = unlockingScript.indexOf("OP_IF");
        if (i == -1) {
            return unlockingScript;
        }
        return unlockingScript.substring(i);
    }
}
