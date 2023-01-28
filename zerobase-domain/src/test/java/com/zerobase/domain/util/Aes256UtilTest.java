package com.zerobase.domain.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Aes256UtilTest {

  @Test
  void encrypt() {
    String encrypt = Aes256Util.encrypt("Hello world");
    assertEquals(Aes256Util.decrypt(encrypt), "Hello world");
  }
}