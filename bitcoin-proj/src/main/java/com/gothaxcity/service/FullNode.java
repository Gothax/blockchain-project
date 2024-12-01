package com.gothaxcity.service;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;
import com.gothaxcity.repository.TransactionRepository;
import com.gothaxcity.repository.UtxoRepository;
import com.gothaxcity.util.SHA256;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class FullNode implements Runnable {

    private static UtxoRepository utxoRepository;
    private static TransactionRepository transactionRepository;
    private static final List<String> performedTxs = new ArrayList<>();
    private static final ReentrantLock lock = new ReentrantLock();


    public FullNode() throws IOException {
        utxoRepository = UtxoRepository.getInstance();
        transactionRepository = TransactionRepository.getInstance();
    }

    public void validateTxs() throws InterruptedException {
        List<Transaction> transactions = transactionRepository.getTransactions();
        for (Transaction transaction : transactions) {
            lock.lock();
            try {
                // 시각적으로 잘 보기 위해 오래 걸린다고 가정
                Thread.sleep(6000);
                processValidate(transaction);
            } finally {
                lock.unlock();
                // busy waiting 하며 다시 시도하는 시간을 기다려준다
                Thread.sleep(50);
            }
        }
    }

    private void processValidate(Transaction transaction) throws InterruptedException {
        String error = "";
        boolean result = true;
        try {
            result = ExecuteEngine.validate(transaction);
        } catch (Exception e) {
            result = false;
            error = e.getMessage();
        }
        System.out.println(transaction.toStringWithValidity(result, error));
        performedTxs.add(transaction.toSnapshotTxt(result));
        if (result) {
            modifyUtxo(transaction);
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


    public static List<String> snapshotTx() {
        lock.lock();
        try {
            return performedTxs;
        } finally {
            lock.unlock();
        }
    }

    public static String snapshotUtxo() {
        lock.lock();
        try {
            List<Utxo> utxoSet = UtxoRepository.getInstance().getUtxoSet();
            StringBuilder sb = new StringBuilder();
            for (Utxo utxo : utxoSet) {
                sb.append(utxo.toTxtAdd());
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            validateTxs();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
