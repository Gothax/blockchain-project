package com.gothaxcity.service;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;
import com.gothaxcity.repository.TransactionRepository;
import com.gothaxcity.repository.UtxoRepository;
import com.gothaxcity.util.SHA256;

import java.io.IOException;
import java.util.List;

public class ExecuteEngine {

    private final UtxoRepository utxoRepository = new UtxoRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public ExecuteEngine() throws IOException {
    }

    public void validateTxs() {
        List<Transaction> transactions = transactionRepository.getTransactions();
        for (Transaction transaction : transactions) {
            String error = "";
            boolean result = true;
            try {
                result = validate(transaction);
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

    private boolean validate(Transaction transaction) {
        return validateAmount(transaction) && validateScript(transaction);
    }

    private boolean validateAmount(Transaction transaction) {
        String inputAmount = transaction.getInput().getAmount();
        List<Utxo> output = transaction.getOutput();

        double inputAmountDouble = Double.parseDouble(inputAmount);
        double outputAmount = output.stream()
                .mapToDouble(amount -> Double.parseDouble(amount.getAmount()))
                .sum();

        return inputAmountDouble >= outputAmount;
    }

    private boolean validateScript(Transaction transaction) {
        // 검증마다 새로운 stack 할당하기 위해 Operator 객체 생성
        String lockingScript = transaction.getInput().getLockingScript();
        String unlockingScript = transaction.getUnlockingScript();
        String prevTxHash = transaction.getInput().getPtxHash();
        Operator operator = new Operator(lockingScript, unlockingScript, prevTxHash);

        return operator.validate();
    }


}
