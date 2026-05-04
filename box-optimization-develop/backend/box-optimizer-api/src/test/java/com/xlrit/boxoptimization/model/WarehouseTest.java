package com.xlrit.boxoptimization.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class WarehouseTest {

  @Test
  void testGetContainersWithEmptyList() {
    Warehouse war = new Warehouse();
    assertTrue(war.getContainers().isEmpty());
  }

  @Test
  void testGetContainersWithSingletonList() {
    ArrayList<Container> contList = new ArrayList<Container>();
    contList.add(new Container());
    Warehouse war = new Warehouse(1, 2, 3, contList);
    assertEquals(contList, war.getContainers());
  }

  @Test
  void testSetContainers() {
    Warehouse war = new Warehouse();
    ArrayList<Container> contList = new ArrayList<Container>();
    war.setContainers(contList);
    assertEquals(contList, war.getContainers());
  }

  @Test
  void testGetUnplacedItemsWithEmptyList() {
    Warehouse war = new Warehouse();
    assertTrue(war.getUnplacedItems().isEmpty());
  }

  @Test
  void testGetUnplacedItemsWithSingletonList() {
    ArrayList<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item("a", 1, 2, 3));
    Warehouse war = new Warehouse();
    war.setUnplacedItems(itemList);
    assertEquals(itemList, war.getUnplacedItems());
  }

  @Test
  void testAddUnplacedItemOnEmptyList() {
    Warehouse war = new Warehouse();
    Item item = new Item();
    war.addUnplacedItem(item);
    assertEquals(item, war.getUnplacedItems().get(0));
  }

  @Test
  void testAddUnplacedItemOnSingletonList() {
    Warehouse war = new Warehouse();
    ArrayList<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item("a", 1, 2, 3));
    war.setUnplacedItems(itemList);
    Item item = new Item("b", 4, 5, 6);
    war.addUnplacedItem(item);
    assertEquals(item, war.getUnplacedItems().get(1));
  }
}
