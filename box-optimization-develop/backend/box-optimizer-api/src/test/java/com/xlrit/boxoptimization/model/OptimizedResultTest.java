package com.xlrit.boxoptimization.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import org.junit.jupiter.api.Test;

public class OptimizedResultTest {
  @Test
  void testGetStatus() {
    OptimizedResult or = new OptimizedResult(new Warehouse(), "a");
    assertEquals("a", or.getStatus());
  }

  @Test
  void testGetWarehouse() {
    Warehouse war = new Warehouse();
    OptimizedResult or = new OptimizedResult(war, "a");
    assertEquals(war, or.getWarehouse());
  }

  @Test
  void testSetStatus() {
    OptimizedResult or = new OptimizedResult(new Warehouse(), "a");
    or.setStatus("abcd");
    assertEquals("abcd", or.getStatus());
  }

  @Test
  void testSetWarehouse() {
    Warehouse war = new Warehouse();
    OptimizedResult or = new OptimizedResult(war, "a");
    Warehouse war2 = new Warehouse(1, 2, 3, new LinkedList<Container>());
    or.setWarehouse(war2);
    assertEquals(war2, or.getWarehouse());
  }
}
