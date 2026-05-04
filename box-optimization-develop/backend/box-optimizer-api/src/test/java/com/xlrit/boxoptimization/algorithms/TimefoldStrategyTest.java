package com.xlrit.boxoptimization.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import com.xlrit.boxoptimization.model.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TimefoldStrategyTest {
  Boolean isValidSolution(Warehouse warehouse) {
    List<Container> contList = warehouse.getContainers();
    for (Container cont : contList) {
      List<Item> iList = cont.getItems();
      for (Item item : iList) {
        if (item.getX() + item.getLength() > warehouse.getLength()
            || item.getY() + item.getHeight() > warehouse.getHeight()
            || item.getZ() + item.getWidth() > warehouse.getWidth()) {
          return false; // box is out of bounds of the warehouse
        }

        // checking if current item overlaps with any other item in the lists
        for (Container otherCont : contList) {
          for (Item otherItem : otherCont.getItems()) {
            if (!item.getId().equals(otherItem.getId())) {
              if (otherItem.getX() > item.getX()
                  && otherItem.getX() < item.getX() + item.getLength()) return false;

              if (otherItem.getY() > item.getY()
                  && otherItem.getY() < item.getY() + item.getHeight()) return false;
              ;

              if (otherItem.getZ() > item.getZ()
                  && otherItem.getZ() < item.getZ() + item.getWidth()) return false;
              ;
            }
          }
        }
      }
    }
    return true;
  }

  Boolean sameAttributes(Item a, Item b) {
    if (!a.getId().equals(b.getId())) return false;

    if (a.getLength() != b.getLength()) return false;

    if (a.getHeight() != b.getHeight()) return false;

    if (a.getWidth() != b.getWidth()) return false;

    return true;
  }

  Boolean sameAttributes(List<Container> a, List<Container> b) {
    if (a.size() != b.size()) return false;

    for (int i = 0; i < a.size(); i++) {
      if (a.get(i).getItems().size() != b.get(i).getItems().size()) return false;

      for (int j = 0; j < a.get(i).getItems().size(); j++) {
        Boolean occurs = false;
        for (Item bItem : b.get(i).getItems()) {
          if (!sameAttributes(a.get(i).getItems().get(j), bItem)) occurs = true;
        }
        if (!occurs) return false;
      }
    }

    return true;
  }

  @Test
  void testEmptyWarehouse() {
    List<Item> itemList = new ArrayList<Item>();
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 5, 6, 7, itemList));
    Warehouse wh = new Warehouse(10, 11, 12, contList);
    TimefoldStrategy tfs = new TimefoldStrategy();
    Warehouse newWh = tfs.solve(wh);

    isValidSolution(newWh);
  }

  @Test
  void testSingleItemSingleContainer() {
    List<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item("a", 1, 2, 3));
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 5, 6, 7, itemList));

    Warehouse wh = new Warehouse(10, 11, 12, contList);
    TimefoldStrategy tfs = new TimefoldStrategy();
    Warehouse newWh = tfs.solve(wh);

    assertTrue(isValidSolution(newWh));
  }

  @Test
  void testTwoItemSingleContainer() {
    List<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item("a", 1, 2, 3));
    itemList.add(new Item("b", 4, 5, 6));
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 5, 6, 7, itemList));

    Warehouse wh = new Warehouse(10, 11, 12, contList);
    TimefoldStrategy tfs = new TimefoldStrategy();
    Warehouse newWh = tfs.solve(wh);

    assertTrue(isValidSolution(newWh));
  }

  @Test
  void testTooBigItemForWarehouse() {
    List<Item> itemList = new ArrayList<Item>();
    Item bigBox = new Item("a", 10, 12, 13);
    itemList.add(bigBox);
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 10, 11, 12, itemList));

    Warehouse wh = new Warehouse(10, 11, 12, contList);
    TimefoldStrategy tfs = new TimefoldStrategy();
    Warehouse newWh = tfs.solve(wh);

    assertTrue(isValidSolution(newWh));
    assertEquals(1, newWh.getUnplacedItems().size());
    assertTrue(sameAttributes(bigBox, newWh.getUnplacedItems().get(0)));
  }

  /* TODO: Timefold does not respect containers
    @Test
    void testTooBigItemForContainer() {
      List<Item> itemList = new ArrayList<Item>();
      Item bigBox = new Item("a", 5, 6, 7);
      itemList.add(bigBox);
      List<Container> contList = new ArrayList<Container>();
      contList.add(new Container("cont1", 1, 2, 3, itemList));

      Warehouse wh = new Warehouse(10, 11, 12, contList);
      TimefoldStrategy tfs = new TimefoldStrategy();
      Warehouse newWh = tfs.solve(wh);

      assertTrue(isValidSolution(newWh));
      assertEquals(1, newWh.getUnplacedItems().size());
      assertTrue(sameAttributes(bigBox, newWh.getUnplacedItems().get(0)));
    }

    @Test
    void respectContainers() {
      List<Item> itemList1 = new ArrayList<Item>();
      Item boxA = new Item("a", 1, 2, 3);
      Item boxB = new Item("b", 1, 1, 1);
      Item boxC = new Item("c", 2, 2, 2);
      itemList1.add(boxA);
      List<Item> itemList2 = new ArrayList<Item>();
      itemList2.add(boxB);
      itemList2.add(boxC);
      List<Container> contList = new ArrayList<Container>();
      contList.add(new Container("cont1", 5, 5, 5, itemList1));
      contList.add(new Container("cont2", 6, 6, 6, itemList1));

      Warehouse wh = new Warehouse(11, 11, 11, contList);
      TimefoldStrategy tfs = new TimefoldStrategy();
      Warehouse newWh = tfs.solve(wh);

      assertTrue(isValidSolution(newWh));
      assertEquals(0, newWh.getUnplacedItems().size());
      assertTrue(sameAttributes(contList, newWh.getContainers()));
    }
  */
}
