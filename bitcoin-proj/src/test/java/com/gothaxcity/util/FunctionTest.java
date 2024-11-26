package com.gothaxcity.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FunctionTest {
    
    @Test
    @DisplayName("hash 함수 test")
    void testHash(){
        // given
        // when

        byte[] hashedHello = Function.hash("hello");
        // then
        System.out.println("hashedHello = " + hashedHello);
    }

}