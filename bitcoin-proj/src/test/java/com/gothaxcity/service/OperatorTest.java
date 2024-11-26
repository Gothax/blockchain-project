package com.gothaxcity.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorTest {

    @Test
    @DisplayName("script를 받아서 스택에 넣는다")
    void pushScriptToStack() {
        // given
        Operator operator = new Operator();

        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
        String unlockingScript = "<sig> <pubKey>";
        // when
        operator.validate(lockingScript, unlockingScript);
        // then
    }


    @Test
    @DisplayName("P2PKH SCRIPT test 실패 사례")
    void testP2PKH() {
        // given
        Operator operator = new Operator();

        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
        String unlockingScript = "<sig> <pubKey>";
        // when
        boolean result = operator.validate(lockingScript, unlockingScript);

        // then
        assertFalse(result);
    }


    @Test
    @DisplayName("P2PKH SCRIPT test 성공 사례")
    void testP2PKH2() {
        // given
        Operator operator = new Operator();

        String lockingScript = "DUP HASH <pubKeyHash> EQUALVERIFY CHECKSIG";
        String unlockingScript = "<sig> <pubKey>";
        // when
        boolean result = operator.validate(lockingScript, unlockingScript);

        // then
        assertTrue(result);
        }
}