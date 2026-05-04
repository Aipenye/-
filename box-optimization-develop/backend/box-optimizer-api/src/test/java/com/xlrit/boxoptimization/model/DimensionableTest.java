package com.xlrit.boxoptimization.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DimensionableTest {

  // Stub subclass for testing
  static class DimensionableStub extends Dimensionable {
    public DimensionableStub(String id, int length, int height, int width) {
      super(id, length, height, width);
    }
  }

  @Test
  void testNegativeHeightNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", 1, 2, -1);
        });
  }

  @Test
  void testGetId() {
    Dimensionable dim = new DimensionableStub("a", 1, 2, 3);
    assertEquals("a", dim.getId());
  }

  @Test
  void testZeroHeightNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", 1, 2, 0);
        });
  }

  @Test
  void testNegativeWidthNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", 1, -1, 3);
        });
  }

  @Test
  void testZeroWidthNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", 1, 0, 3);
        });
  }

  @Test
  void testNegativeLengthNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", -1, 2, 3);
        });
  }

  @Test
  void testZeroLengthNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Dimensionable dim = new DimensionableStub("a", 0, 2, 3);
        });
  }

  @Test
  void testGetHeight() {
    Dimensionable dim = new DimensionableStub("a", 1, 2, 3);
    assertEquals(2, dim.getHeight());
  }

  @Test
  void testGetWidth() {
    Dimensionable dim = new DimensionableStub("a", 1, 2, 3);
    assertEquals(3, dim.getWidth());
  }

  @Test
  void testGetLength() {
    Dimensionable dim = new DimensionableStub("a", 1, 2, 3);
    assertEquals(1, dim.getLength());
  }

  @Test
  void testGetVolume() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertEquals(60, dim.getVolume());
  }

  @Test
  void testSetHeightTo1() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    dim.setHeight(1);
    assertEquals(1, dim.getHeight());
  }

  @Test
  void testSetZeroHeightNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setHeight(0);
        });
  }

  @Test
  void testSetNegativeHeightNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setHeight(-1);
        });
  }

  @Test
  void testSetWidthTo1() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    dim.setWidth(1);
    assertEquals(1, dim.getWidth());
  }

  @Test
  void testSetZeroWidthNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setWidth(0);
        });
  }

  @Test
  void testSetNegativeWidthNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setWidth(-1);
        });
  }

  @Test
  void testSetLengthTo1() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    dim.setLength(1);
    assertEquals(1, dim.getLength());
  }

  @Test
  void testSetZeroLengthNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setLength(0);
        });
  }

  @Test
  void testSetNegativeLengthNotAllowed() {
    Dimensionable dim = new DimensionableStub("a", 10, 2, 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dim.setLength(-1);
        });
  }
}
