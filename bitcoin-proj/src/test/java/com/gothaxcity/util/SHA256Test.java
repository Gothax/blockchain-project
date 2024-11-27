package com.gothaxcity.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SHA256Test {
    
    @Test
    @DisplayName("hash 함수 test")
    void testHashWithSHA256(){
        // given
        // when

        String hashedHello = SHA256.encrypt("hello");
        // then
        System.out.println("hashedHello = " + hashedHello);
    }

}