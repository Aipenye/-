package com.xlrit.boxoptimization.mappers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.boxpackaging.SkjolberSolver;
import com.github.skjolber.packing.api.Stackable;
import com.github.skjolber.packing.api.StackableItem;
import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import com.xlrit.boxoptimization.testutil.*;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SkjolberMapperTest {
  Boolean isEqual(SkjolberSolver a, SkjolberSolver b) {
    SkjolbergContainerWrapperList aWrapper = new SkjolbergContainerWrapperList(a.getContainers());
    SkjolbergContainerWrapperList bWrapper = new SkjolbergContainerWrapperList(b.getContainers());

    if (!aWrapper.equals(bWrapper)) return false;

    List<StackableItem> aItems = a.getBoxes();
    List<StackableItem> bItems = b.getBoxes();

    if (aItems == null || bItems == null) {
      if (aItems == null && bItems == null) {
        return true;
      } else return false;
    }
    if (aItems.size() != bItems.size()) return false;

    for (int i = 0; i < aItems.size(); i++) {
      StackableItem aCurrent = aItems.get(i);
      StackableItem bCurrent = bItems.get(i);
      Stackable aBox = aCurrent.getStackable();
      Stackable bBox = bCurrent.getStackable();

      if (aBox.getVolume() != bBox.getVolume()
          || aBox.getDescription() != bBox.getDescription()
          || aBox.getWeight() != bBox.getWeight()
          || aCurrent.getCount() != bCurrent.getCount()) return false;
    }

    return true;
  }

  @Test
  void testWarehouseToProblem_EmptyWarehouse() {
    Warehouse war = new Warehouse();
    SkjolberSolver ss = new SkjolberSolver();

    SkjolberSolver result = SkjolberMapper.warehouseToProblem(war);

    assertTrue(isEqual(ss, result));
  }

  @Test
  void testWarehouseToProblem_SingleItem() {
    List<Item> itemList1 = new LinkedList<>();
    Item item1 = new Item("a", 1, 2, 3);
    itemList1.add(item1);

    List<Container> contList = new LinkedList<>();
    Container cont1 = new Container("cont1", 4, 6, 5, itemList1);
    contList.add(cont1);

    Warehouse war = new Warehouse(7, 8, 9, contList);

    SkjolberSolver ss = new SkjolberSolver();
    ss.addContainer("cont1", 4, 5, 6);
    ss.addBox("a", 1, 3, 2);

    SkjolberSolver result = SkjolberMapper.warehouseToProblem(war);

    assertTrue(isEqual(ss, result));
  }

  @Test
  void testWarehouseToProblem_TripleItem() {
    List<Item> itemList1 = new LinkedList<>();
    Item item1 = new Item("a", 1, 1, 1);
    Item item2 = new Item("b", 2, 2, 2);
    Item item3 = new Item("c", 3, 3, 3);
    itemList1.add(item1);
    itemList1.add(item2);
    itemList1.add(item3);

    List<Container> contList = new LinkedList<>();
    Container cont1 = new Container("cont1", 4, 5, 6, itemList1);
    contList.add(cont1);

    Warehouse war = new Warehouse(7, 8, 9, contList);

    SkjolberSolver ss = new SkjolberSolver();
    ss.addContainer("cont1", 4, 5, 6);
    ss.addBox("a", 1, 1, 1);
    ss.addBox("b", 2, 2, 2);
    ss.addBox("c", 3, 3, 3);

    SkjolberSolver result = SkjolberMapper.warehouseToProblem(war);

    assertTrue(isEqual(ss, result));
  }

  @Test
  void testWarehouseToProblem_MultipleContainer() {
    List<Item> itemList1 = new LinkedList<>();
    List<Item> itemList2 = new LinkedList<>();
    Item item1 = new Item("a", 1, 1, 1);
    Item item2 = new Item("b", 2, 2, 2);
    Item item3 = new Item("c", 3, 3, 3);
    itemList1.add(item1);
    itemList1.add(item2);
    itemList2.add(item3);

    List<Container> contList = new LinkedList<>();
    Container cont1 = new Container("cont1", 4, 5, 6, itemList1);
    Container cont2 = new Container("cont2", 6, 4, 5, itemList2);
    contList.add(cont1);
    contList.add(cont2);

    Warehouse war = new Warehouse(7, 8, 9, contList);

    SkjolberSolver ss = new SkjolberSolver();
    ss.addContainer("cont1", 4, 5, 6);
    ss.addContainer("cont2", 6, 4, 5);
    ss.addBox("a", 1, 1, 1);
    ss.addBox("b", 2, 2, 2);
    ss.addBox("c", 3, 3, 3);

    SkjolberSolver result = SkjolberMapper.warehouseToProblem(war);

    assertTrue(isEqual(ss, result));
  }
}
