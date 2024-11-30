package com.gothaxcity.domain;

import com.gothaxcity.util.SHA256;

import java.util.List;

public class Transaction {
    private final Utxo input;
    private final List<Utxo> output;
    private final String unlockingScript;

    public Transaction(Utxo input, List<Utxo> output, String unlockingScript) {
        this.input = input;
        this.output = output;
        this.unlockingScript = unlockingScript;
    }

    public String getUnlockingScript() {
        return unlockingScript;
    }

    public Utxo getInput() {
        return input;
    }

    public List<Utxo> getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "Transaction{" + "\n" +
                "input=" + input + "\n" +
                "output=" + output + "\n" +
                "unlockingScript='" + unlockingScript + '\'' + "\n" +
                '}';
    }

    public String toHashTxt() {
        return "input: " + input.toPlainString() + "," + unlockingScript + "\n" + outputToTxt();
    }

    private String outputToTxt() {
        StringBuilder sb = new StringBuilder();
        for (Utxo utxo : output) {
            sb.append("output: ").append(utxo.toHashTxt()).append("\n");
        }
        return sb.toString();
    }

    private String outputWithTermTxt() {
        StringBuilder sb = new StringBuilder();
        for (Utxo utxo : output) {
            sb.append("    output: ").append(utxo.toHashTxt()).append("\n");
        }
        return sb.toString();
    }


    public String toStringWithValidity(boolean result, String failedAt) {
        String valid = result ? "passed" : "failed";
        StringBuilder sb = new StringBuilder();

        sb.append("transaction: ").append(SHA256.encryptGetEncode(this.toHashTxt())).append("\n")
                .append("    input=").append(input.toPlainString()).append(", ").append(unlockingScript).append("\n")
                .append(outputWithTermTxt())
                .append("    validity check: ").append(valid);

        // 검증 실패 시만 "failed at" 추가
        if (!result && failedAt != null && !failedAt.isEmpty()) {
            sb.append("\n        failed at: ").append(failedAt);
        }

        sb.append("\n");
        return sb.toString();
    }


    public String toSnapshotTxt(boolean result) {
        String valid = result ? "passed" : "failed";
        return "transaction: " + SHA256.encryptGetEncode(this.toHashTxt()) +
                ", validity check:" + valid;
    }
}
