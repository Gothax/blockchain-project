package com.gothaxcity.domain;

public class Utxo {

    private final String ptxHash;
    private final String outputIndex;
    private final String amount;
    private final String script;

    public Utxo(String ptxHash, String outputIndex, String amount, String script) {
        this.ptxHash = ptxHash;
        this.outputIndex = outputIndex;
        this.amount = amount;
        this.script = script;
    }

    public String getPtxHash() {
        return ptxHash;
    }

    public String getOutputIndex() {
        return outputIndex;
    }

    public String getAmount() {
        return amount;
    }

    public String getScript() {
        return script;
    }

    @Override
    public String toString() {
        return "Utxo{" +
                "txHash='" + ptxHash + '\'' +
                ", outputIndex=" + outputIndex +
                ", value=" + amount +
                ", script=" + script +
                '}';
    }

    public String toTxt() {
        return "\n" + ptxHash + "," + outputIndex + "," + amount + "," + script;
    }

    public String toPlainString() {
        return ptxHash + "," + outputIndex + "," + amount + "," + script;
    }
}
