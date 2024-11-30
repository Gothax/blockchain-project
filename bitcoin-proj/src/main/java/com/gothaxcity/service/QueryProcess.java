package com.gothaxcity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class QueryProcess implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(QueryProcess.class);

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                logger.debug("current thread: {}", Thread.currentThread().getName());
                queryTransactions();
                queryUtxos();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void queryTransactions() {
        List<String> transactionSnapshot = FullNode.snapshotTx();
        System.out.println("=========snapshot transactions=========");
        transactionSnapshot.forEach(System.out::println);
        System.out.println("================================");
    }

    private void queryUtxos() {
        String utxoSnapshot = FullNode.snapshotUtxo();
        String[] split = utxoSnapshot.split("Utxo");
        System.out.println("=========snapshot utxo=========");
        for (String s : split) {
            System.out.println(s);
        }
        System.out.println("================================" + "\n\n");
    }

}
