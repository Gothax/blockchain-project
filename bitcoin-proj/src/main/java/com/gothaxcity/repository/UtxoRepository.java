package com.gothaxcity.repository;

import com.gothaxcity.domain.Utxo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

public class UtxoRepository {
    private static final Path PATH = Paths.get("src/main/resources/UTXOes.txt");
    private static final List<Utxo> utxoSet = new ArrayList<>();

    public UtxoRepository() throws IOException {
        loadUtxoSet();
    }

    public List<Utxo> getUtxoSet() {
        return utxoSet;
    }

    public static Utxo findUtxoByIdAndIndex(String id, String outputIndex) {
        return utxoSet.stream()
                .filter(utxo -> utxo.getPtxHash().equals(id) && utxo.getOutputIndex().strip().equals(outputIndex))
                .findFirst()
                .orElse(null);
    }

    public static void addUtxo(Utxo utxo) {
        utxoSet.add(utxo);
        addToTxt(utxo);
    }

    public void removeUtxo(Utxo utxo) {
        utxoSet.remove(utxo);
        removeTxt(utxo);
    }

    private void removeTxt(Utxo utxo) {
        try {
            List<String> strings = Files.readAllLines(PATH);
            List<String> collected = strings.stream()
                    .filter(line -> !line.contains(utxo.toTxt().strip()))
                    .toList();
            Files.write(PATH, collected, WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToTxt(Utxo utxo) {
        try {
            Files.write(PATH, utxo.toTxt().getBytes(), APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUtxoSet() throws IOException {
        List<String> strings = Files.readAllLines(PATH);
        for (String string : strings) {
            String[] split = string.split(",");
            Utxo utxo = new Utxo(split[0], split[1], split[2], split[3]);
            utxoSet.add(utxo);
        }
    }


}
