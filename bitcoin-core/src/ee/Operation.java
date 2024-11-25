package ee;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;

public class Operation {

    public static ArrayDeque<Object> duplicate(ArrayDeque<Object> stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("스택이 비어있습니다.");
        }
        stack.push(stack.peek());
        return stack;
    }

    public static ArrayDeque<String> toHash(ArrayDeque<String> stack) throws NoSuchAlgorithmException {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("스택이 비어있습니다.");
        }
        String popped = stack.pop();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        return stack;
    }

    public static ArrayDeque<String> checkEqual(ArrayDeque<String> stack) {
        String popped1 = stack.pop();
        String popped2 = stack.pop();
        stack.addFirst(String.valueOf(popped1.equals(popped2)));
        return stack;
    }

    public static ArrayDeque<String> checkEqualVerify(ArrayDeque<String> stack) {
        return stack;
    }

    public static ArrayDeque<String> checkSignature(ArrayDeque<String> stack) {
        return stack;
    }

    public static ArrayDeque<String> checkSigAndVerify(ArrayDeque<String> stack) {
        return stack;
    }

    public static ArrayDeque<String> checkMultiSig(ArrayDeque<String> stack) {
        return stack;
    }

    public static ArrayDeque<String> checkMultiSigAndVerify(ArrayDeque<String> stack) {
        return stack;
    }


}
