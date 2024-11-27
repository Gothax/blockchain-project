package com.gothaxcity.domain;

public class Utxo {

    private final String ptxHash;
    private final String outputIndex;
    private final String amount;
    private final String lockingScript;

    public Utxo(String ptxHash, String outputIndex, String amount, String lockingScript) {
        this.ptxHash = ptxHash;
        this.outputIndex = outputIndex;
        this.amount = amount;
        this.lockingScript = lockingScript;
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

    public String getLockingScript() {
        return lockingScript;
    }

    @Override
    public String toString() {
        return "Utxo{" +
                "txHash='" + ptxHash + '\'' +
                ", outputIndex=" + outputIndex +
                ", value=" + amount +
                ", script=" + lockingScript +
                '}';
    }

    public String toTxt() {
        return "\n" + ptxHash + "," + outputIndex + "," + amount + "," + lockingScript;
    }

    public String toPlainString() {
        return ptxHash + "," + outputIndex + "," + amount + "," + lockingScript;
    }

    public String toHashTxt() {
        return outputIndex + ", " + amount + ", " + lockingScript;
    }
}
