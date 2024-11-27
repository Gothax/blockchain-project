package com.gothaxcity.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SHA256Test {
    
    @Test
    @DisplayName("hash 함수 test")
    void testHashWithSHA256(){
        // given
        // when

        String hashedHello = SHA256.encryptGetEncode("hello");
        // then
        System.out.println("hashedHello = " + hashedHello);
    }


    @Test
    @DisplayName("같은 input은 같은 hash output")
    void hashFunctionTest() {
        // given
        byte[] bytes1 = SHA256.encryptGetBytes("hello");
        byte[] bytes2 = SHA256.encryptGetBytes("hello");

        String s1 = SHA256.encryptGetEncode("hello encrypt with string");
        String s2 = SHA256.encryptGetEncode("hello encrypt with string");
        // when
        System.out.println("s1 = " + s1);
        System.out.println("s2 = " + s2);
        // then
        assertArrayEquals(bytes1, bytes2);
        assertEquals(s1, s2);
    }

}