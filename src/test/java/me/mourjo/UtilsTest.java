package me.mourjo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  public void testAsciiFolding() {
    assertEquals("abcd", Utils.asciiFold("abcd"));
    assertEquals("josejuanbarea", Utils.asciiFold("Jose Juan Barea"));
    assertEquals("ramonvallarino", Utils.asciiFold("Ramón Vallarino"));
    assertEquals("jangkangheo", Utils.asciiFold("Jang-kang Heo"));
    assertEquals("scifi", Utils.asciiFold("sci-fi"));
  }
}