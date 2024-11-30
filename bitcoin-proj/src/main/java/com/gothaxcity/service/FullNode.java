package com.gothaxcity.service;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;
import com.gothaxcity.repository.TransactionRepository;
import com.gothaxcity.repository.UtxoRepository;
import com.gothaxcity.util.SHA256;

import java.io.IOException;
import java.util.List;

public class FullNode {

    private final UtxoRepository utxoRepository = new UtxoRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public FullNode() throws IOException {
    }

    public void validateTxs() throws InterruptedException {
        List<Transaction> transactions = transactionRepository.getTransactions();
        for (Transaction transaction : transactions) {
            String error = "";
            boolean result = true;
            try {
                result = ExecuteEngine.validate(transaction);
            } catch (Exception e) {
                result = false;
                error = e.getMessage();
                e.printStackTrace();
            }
            System.out.println(transaction.toStringWithValidity(result, error));
            if (result) {
                modifyUtxo(transaction);
            }
        }
    }

    private void modifyUtxo(Transaction transaction) {
        Utxo input = transaction.getInput();
        UtxoRepository.removeUtxo(input);

        List<Utxo> output = transaction.getOutput();
        for (Utxo newUtxo : output) {
            String txIdHash = SHA256.encryptGetEncode(transaction.toHashTxt());
            newUtxo.addPtxHash(txIdHash);
            UtxoRepository.addUtxo(newUtxo);
        }
    }
}
