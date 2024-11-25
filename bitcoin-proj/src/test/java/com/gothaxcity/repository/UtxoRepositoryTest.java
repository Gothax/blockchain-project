package com.gothaxcity.repository;

import com.gothaxcity.domain.Utxo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtxoRepositoryTest {
    private final UtxoRepository utxoRepository = new UtxoRepository();

    UtxoRepositoryTest() throws IOException {
    }

    @Test
    @DisplayName("txt 읽기")
    void readFile() throws IOException {
        // given
        List<String> strings = Files.readAllLines(Paths.get("src/main/resources/UTXOes.txt"));
        // when
        strings.forEach(System.out::println);
        // then
        assertNotNull(strings);
    }


    @Test
    @DisplayName("txt 읽어서 객체로 변환")
    void makeEntity() throws IOException {
        // given
        List<String> strings = Files.readAllLines(Paths.get("src/main/resources/UTXOes.txt"));
        // when
        // then
        for (String string : strings) {
            String[] split = string.split(",");
            Utxo utxo = new Utxo(split[0], split[1], split[2], split[3]);
            assertNotNull(utxo);
            System.out.println(utxo);
        }
    }

    @Test
    @DisplayName("생성한 객체 utxo set 순서가 txt와 일치해야함")
    void orderOKTest() throws IOException {
        // given
        UtxoRepository utxoRepository = new UtxoRepository();
        // when
        List<Utxo> utxoSet = utxoRepository.getUtxoSet();
        // then
        int i = 1;
        for (Utxo utxo : utxoSet) {
            String id = utxo.getPtxHash();
            assertEquals(String.valueOf("tx"+i), id);
            i++;
        }
    }

    @Test
    @DisplayName("추가한 객체를 txt파일에 write")
    void write2Txt() {
        // given
        Utxo newUtxo = new Utxo("tx4", "output1", "output2", "output3");
        utxoRepository.addUtxo(newUtxo);
        // when

        // then
    }

    @Test
    @DisplayName("객체를 삭제하면 txt파일에서도 삭제")
    void deleteAtTxt() {
        utxoRepository.getUtxoSet().forEach(System.out::println);
        // given
        Utxo delTx = utxoRepository.findUtxoByIdAndIndex("tx4", "0");
        // when
        utxoRepository.removeUtxo(delTx);
        utxoRepository.getUtxoSet().forEach(System.out::println);
        // then
    }
}