package com.gothaxcity.repository;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private static final Path PATH = Paths.get("src/main/resources/transactions.txt");
    private final List<Transaction> transactionSet = new ArrayList<>();

    public TransactionRepository() throws IOException {
        loadTransactionSet();
    }

    private void loadTransactionSet() throws IOException {
        List<String> strings = Files.readAllLines(PATH);
        Utxo input = null;
        List<Utxo> output = new ArrayList<>();

        for (String string : strings) {
            if (string.startsWith("input")) {
                input = getInput(string);
            }
//            if (string.startsWith("output")) {
//                getOutput(string, output);
//            }
            Transaction transaction = new Transaction(input, output);
            transactionSet.add(transaction);
        }
    }

//    private void getOutput(String string, List<Utxo> output) {
//        String[] split = string.split(",");
//        String[] indexAndAmount = split[1].split(":");
//        String index = indexAndAmount[0].strip();
//        String amount = indexAndAmount[1].strip();
//        String script = split[2].strip();
//        // TODO: SHA 256 - TX id 생성
//        Utxo added = new Utxo("임시", index, amount, script);
//        UtxoRepository.addUtxo(added);
//        output.add(added);
//    }

    private Utxo getInput(String string) {
        Utxo input;
        String[] split = string.split(",");
        String id = split[0];
        String outputIndex = split[1];
        input = UtxoRepository.findUtxoByIdAndIndex(id, outputIndex);
        return input;
    }
}
