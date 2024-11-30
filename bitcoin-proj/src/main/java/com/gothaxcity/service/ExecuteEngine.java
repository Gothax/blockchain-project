package com.gothaxcity.service;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;

import java.util.List;

public class ExecuteEngine {

    public static boolean validate(Transaction transaction) {
        return validateAmount(transaction) && validateScript(transaction);
    }

    private static boolean validateAmount(Transaction transaction) {
        String inputAmount = transaction.getInput().getAmount();
        List<Utxo> output = transaction.getOutput();

        double inputAmountDouble = Double.parseDouble(inputAmount);
        double outputAmount = output.stream()
                .mapToDouble(amount -> Double.parseDouble(amount.getAmount()))
                .sum();

        return inputAmountDouble >= outputAmount;
    }

    private static boolean validateScript(Transaction transaction) {
        // 검증마다 새로운 stack 할당하기 위해 Operator 객체 생성
        String lockingScript = transaction.getInput().getLockingScript();
        String unlockingScript = transaction.getUnlockingScript();
        String prevTxHash = transaction.getInput().getPtxHash();
        Operator operator = new Operator(lockingScript, unlockingScript, prevTxHash);

        return operator.validate();
    }

}
