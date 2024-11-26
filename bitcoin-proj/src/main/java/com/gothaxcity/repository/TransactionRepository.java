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
    private final List<Transaction> transactions = new ArrayList<>();

    public TransactionRepository() throws IOException {
        loadTransactionSet();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private void loadTransactionSet() throws IOException {
        List<String> strings = Files.readAllLines(PATH);
        Utxo input = null;
        List<Utxo> output = new ArrayList<>();

        for (String string : strings) {
            if (string.startsWith("input")) {
                input = getInput(string);
            }
            if (string.startsWith("output")) {
                getOutput(string, output);
            }
            if (string.isEmpty()) {
                Transaction transaction = new Transaction(input, output);
                transactions.add(transaction);
                input = null;
                output = new ArrayList<>();
            }
        }
    }

    private void getOutput(String string, List<Utxo> output) {
        String[] split = string.split(",");
        String[] indexAndAmount = split[1].split(":");
        String index = indexAndAmount[0].strip();
        String amount = indexAndAmount[1].strip();
        String script = split[2].strip();
        // utxoSet 추가할때 id 생성
        Utxo added = new Utxo("temp", index, amount, script);
        // TODO: 검증 후 valid -> output utxo를 utxoSet에 추가 -> tx id 생성 (output add + SHA256 / input remove)
        output.add(added);
    }

    private Utxo getInput(String string) {
        Utxo input;
        String[] split = string.split(",");

        String[] inputAndId = split[0].split(":");
        String id = inputAndId[1];
        String outputIndex = split[1];

        input = UtxoRepository.findUtxoByIdAndIndex(id, outputIndex);
        return input;
    }
}
