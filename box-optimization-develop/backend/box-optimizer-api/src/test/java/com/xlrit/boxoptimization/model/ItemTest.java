package com.xlrit.boxoptimization.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ItemTest {
  @Test
  void testGetX() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    assertEquals(4, item.getX());
  }

  @Test
  void testGetY() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    assertEquals(5, item.getY());
  }

  @Test
  void testGetZ() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    assertEquals(6, item.getZ());
  }

  @Test
  void testSetX() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    item.setX(-1);
    assertEquals(-1, item.getX());
  }

  @Test
  void testSetY() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    item.setY(-1);
    assertEquals(-1, item.getY());
  }

  @Test
  void testSetZ() {
    Item item = new Item("a", 1, 2, 3, 4, 5, 6);
    item.setZ(-1);
    assertEquals(-1, item.getZ());
  }
}
