package com.gothaxcity.domain;

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
            sb.append("output: ").append(utxo.toPlainString()).append("\n");
        }
        return sb.toString();
    }
}
