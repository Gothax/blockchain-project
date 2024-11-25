package com.gothaxcity.repository;

import com.gothaxcity.domain.Utxo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private final TransactionRepository transactionRepository = new TransactionRepository();

    TransactionRepositoryTest() throws IOException {
    }

    @Test
    @DisplayName("txt 파일 읽기")
    void readFromTxt() throws IOException {
        // given
        List<String> strings = Files.readAllLines(Paths.get("src/main/resources/transactions.txt"));
        // when
        strings.forEach(System.out::println);
        // then
    }


    @Test
    @DisplayName("txt 파일 읽고 객체로 반환-input은 not null, output은 생성")
    void makeEntity() throws IOException {
        // given
        new UtxoRepository();
        List<String> strings = Files.readAllLines(Paths.get("src/main/resources/transactions.txt"));
        // when
        for (String string : strings) {
            if (string.startsWith("input")) {
                String[] split = string.split(",");
                String[] inputAndId = split[0].split(":");
                String id = inputAndId[1].strip();
                String outputIndex = split[1].strip();
                String script = split[2];
                System.out.println("================================");
                System.out.println("id = " + id);
                System.out.println("outputIndex = " + outputIndex);
                System.out.println("script = " + script);
                Utxo utxoByIdAndIndex = UtxoRepository.findUtxoByIdAndIndex(id, outputIndex);
                System.out.println("utxoByIdAndIndex = " + utxoByIdAndIndex);

                assertNotNull(utxoByIdAndIndex);
            }
            if (string.startsWith("output")) {
                String[] split = string.split(",");
                String[] indexAndAmount = split[1].split(":");
                String index = indexAndAmount[0].strip();
                String amount = indexAndAmount[1].strip();
                String script = split[2].strip();

                System.out.println("================================");
                System.out.println("index = " + index);
                System.out.println("amount = " + amount);
                System.out.println("script = " + script);

                Utxo added = new Utxo("현재 트랜잭션 id", index, amount, script);
                System.out.println("added = " + added);
            }
        }
        // then
    }




}