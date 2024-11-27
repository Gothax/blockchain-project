package com.gothaxcity.service;

import com.gothaxcity.domain.Transaction;
import com.gothaxcity.domain.Utxo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class ExecuteEngineTest {

    @Test
    @DisplayName("input amount가 output amount의 합보다 커야한다")
    void inputAmountGreaterThanOutput() throws IOException {
        // given
        Utxo input = new Utxo("test", "0", "10", "testlocking");
        Utxo output1 = new Utxo("test2", "0", "1", "testlocking");
        Utxo output2 = new Utxo("test2", "1", "3", "testlocking");
        List<Utxo> outputs = List.of(output1, output2);
        new Transaction(input, outputs, "testUnlocking");
        // when

        ExecuteEngine executeEngine = new ExecuteEngine();
//        executeEngine.validateTxs();
        // then

    }

}