package com.xlrit.boxoptimization.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class ContainerTest {

  @Test
  void testGetItemsWithEmptyList() {
    Container cont = new Container("a", 1, 2, 3);
    assertTrue(cont.getItems().isEmpty());
  }

  @Test
  void testGetItemsWithSingletonList() {
    ArrayList<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item());
    Container cont = new Container("a", 1, 2, 3, itemList);
    assertEquals(itemList, cont.getItems());
  }

  @Test
  void testGetId() {
    Container cont = new Container("a", 1, 2, 3);
    assertEquals("a", cont.getId());
  }

  @Test
  void testSetItemsWithEmptyList() {
    ArrayList<Item> itemList = new ArrayList<Item>();
    Container cont = new Container("a", 1, 2, 3);
    cont.setItems(itemList);
    assertEquals(itemList, cont.getItems());
  }

  @Test
  void testSetItemsWithSingletonList() {
    ArrayList<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item());
    Container cont = new Container("a", 1, 2, 3);
    cont.setItems(itemList);
    assertEquals(itemList, cont.getItems());
  }

  @Test
  void testAddBoxOnEmptyList() {
    Container cont = new Container("a", 1, 2, 3);
    Item box = new Item();
    cont.addBox(box);
    assertEquals(box, cont.getItems().get(0));
  }

  @Test
  void testAddBoxOnExistingList() {
    ArrayList<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item());
    Container cont = new Container("a", 1, 2, 3, itemList);
    Item box = new Item();
    cont.addBox(box);
    assertEquals(box, cont.getItems().get(1));
  }
}
