package com.gothaxcity.service;

import java.util.ArrayDeque;

import static java.util.Arrays.stream;

public class Operator {

    private final ArrayDeque<Object> stack = new ArrayDeque<>();

    public boolean validate(String lockingScript, String unlockingScript) {

        stream(unlockingScript.split(" ")).toList().forEach(stack::push);
        System.out.println("stack = " + stack);

        try {
            stream(lockingScript.split(" ")).forEach(this::execute);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return stack.pop().equals(true) && stack.isEmpty();
    }

    private void execute(String s) {
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

            Object pubKey = stack.pop();
            Object sig = stack.pop();

            
        }

        stack.push(s);
        System.out.println("stack = " + stack);
    }

    private boolean equals() {
        return stack.pop().equals(stack.pop());
    }

    private void hash() {


    }
}
