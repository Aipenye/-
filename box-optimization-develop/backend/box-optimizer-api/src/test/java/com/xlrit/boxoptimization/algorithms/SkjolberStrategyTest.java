package com.xlrit.boxoptimization.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SkjolberStrategyTest {

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
    Warehouse wh = new Warehouse(10, 11, 12, new ArrayList<Container>());
    SkjolberStrategy ss = new SkjolberStrategy();
    assertThrows(
        IllegalStateException.class,
        () -> {
          Warehouse newWh = ss.solve(wh);
        });
  }

  @Test
  void testSingleItemSingleContainer() {
    List<Item> itemList = new ArrayList<Item>();
    itemList.add(new Item("a", 1, 2, 3));
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 5, 6, 7, itemList));

    Warehouse wh = new Warehouse(10, 11, 12, contList);
    SkjolberStrategy ss = new SkjolberStrategy();
    Warehouse newWh = ss.solve(wh);

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
    SkjolberStrategy ss = new SkjolberStrategy();
    Warehouse newWh = ss.solve(wh);

    assertTrue(isValidSolution(newWh));
  }

  // * TODO: Test currently fails because solutionToWarehouse fails as solution is null
  // @Test
  // void testTooBigItemForWarehouse() {
  //   List<Item> itemList = new ArrayList<Item>();
  //   Item bigBox = new Item("a", 10, 12, 13);
  //   itemList.add(bigBox);
  //   List<Container> contList = new ArrayList<Container>();
  //   contList.add(new Container("cont1", 10, 11, 12, itemList));

  //   Warehouse wh = new Warehouse(10, 11, 12, contList);
  //   SkjolberStrategy ss = new SkjolberStrategy();
  //   Warehouse newWh = ss.solve(wh);

  //   assertTrue(isValidSolution(newWh));
  //   assertEquals(1, newWh.getUnplacedItems().size());
  //   assertTrue(sameAttributes(bigBox, newWh.getUnplacedItems().get(0)));
  // }

  /* Does not respect containers yet
  @Test
  void testTooBigItemForContainer() {
    List<Item> itemList = new ArrayList<Item>();
    Item bigBox = new Item("a", 5, 6, 7);
    itemList.add(bigBox);
    List<Container> contList = new ArrayList<Container>();
    contList.add(new Container("cont1", 1, 2, 3, itemList));

    Warehouse wh = new Warehouse(10, 11, 12, contList);
    SkjolberStrategy ss = new SkjolberStrategy();
    Warehouse newWh = ss.solve(wh);

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
    SkjolberStrategy ss = new SkjolberStrategy();
    Warehouse newWh = ss.solve(wh);

    assertTrue(isValidSolution(newWh));
    assertEquals(0, newWh.getUnplacedItems().size());
    assertTrue(sameAttributes(contList, newWh.getContainers()));
  }

  */

  @Test
  public void testOptimize_withOneItem_shouldReturnOptimizedWarehouse() {
    // Build the warehouse to test
    Item item = new Item("helloItem", 7, 4, 1);
    List<Item> listItem = new ArrayList<>(List.of(item));

    Container container = new Container("helloContainer", 10, 10, 10, listItem);
    List<Container> listContainer = new ArrayList<>(List.of(container));

    Warehouse inputWarehouse = new Warehouse(10, 10, 10, listContainer);

    SkjolberStrategy strategy = new SkjolberStrategy();

    Warehouse resultWarehouse = strategy.solve(inputWarehouse);

    // Expected result

    Item expectedItem = new Item(item);
    expectedItem.setX(0);
    expectedItem.setY(0);
    expectedItem.setZ(0);
    expectedItem.setPlaced(true);

    System.out.println("The X is:" + expectedItem.getX());
    List<Item> expectedListItem = new ArrayList<>(List.of(expectedItem));

    Container expectedContainer = new Container("helloContainer", 10, 10, 10, expectedListItem);
    List<Container> expectedListContainer = new ArrayList<>(List.of(expectedContainer));

    Warehouse expectedWarehouse = new Warehouse(10, 10, 10, expectedListContainer);

    // Assert
    assertNotNull(resultWarehouse);
    assertEquals(expectedWarehouse, resultWarehouse);
  }

  @Test
  public void testOptimize_withTwoItem_shouldReturnOptimizedWarehouse() {
    // Build the warehouse to test
    Item item = new Item("helloItem", 7, 4, 1);
    List<Item> listItem = new ArrayList<>(List.of(item, item));

    Container container = new Container("helloContainer", 10, 10, 10, listItem);
    List<Container> listContainer = new ArrayList<>(List.of(container));

    Warehouse inputWarehouse = new Warehouse(10, 10, 10, listContainer);

    SkjolberStrategy strategy = new SkjolberStrategy();

    Warehouse resultWarehouse = strategy.solve(inputWarehouse);

    // Expected result

    Item expectedItem1 = new Item(item);
    expectedItem1.setX(0);
    expectedItem1.setY(0);
    expectedItem1.setZ(0);
    expectedItem1.setPlaced(true);

    Item expectedItem2 = new Item(item);
    expectedItem2.setX(0);
    expectedItem2.setY(0);
    expectedItem2.setZ(4);
    expectedItem2.setPlaced(true);

    List<Item> expectedListItem = new ArrayList<>(List.of(expectedItem1, expectedItem2));

    Container expectedContainer = new Container("helloContainer", 10, 10, 10, expectedListItem);
    List<Container> expectedListContainer = new ArrayList<>(List.of(expectedContainer));

    Warehouse expectedWarehouse = new Warehouse(10, 10, 10, expectedListContainer);

    System.out.println("this is the result" + resultWarehouse.toString());
    // Assert
    assertNotNull(resultWarehouse);
    assertEquals(expectedWarehouse, resultWarehouse);
  }
}
