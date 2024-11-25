package com.gothaxcity.domain;

import java.util.List;

public class Transaction {
    private final Utxo input;
    private final List<Utxo> output;

    public Transaction(Utxo input, List<Utxo> output) {
        this.input = input;
        this.output = output;
    }
}
