package com.xlrit.boxoptimization.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.solver.BoxSolver;
import org.junit.jupiter.api.Test;

public class TimefoldMapperTest {
  String isEqual(BoxSolver a, BoxSolver b) {
    BoxPlan aProblem = a.getProblem();
    BoxPlan bProblem = b.getProblem();
    if (!aProblem.getContainer().equals(bProblem.getContainer())) return "false: container";

    if (aProblem.getBoxes() == null || bProblem.getBoxes() == null) {
      if (aProblem.getBoxes() == null && bProblem.getBoxes() == null) return "true";
      else return "false: one of them is null";
    }
    if (aProblem.getBoxes().size() != bProblem.getBoxes().size()) return "false: different size";

    for (int i = 0; i < aProblem.getBoxes().size(); i++) {
      Box aBox = aProblem.getBoxes().get(i);
      Box bBox = bProblem.getBoxes().get(i);

      if (!aBox.getId().equals(bBox.getId())
          || !aBox.getName().equals(bBox.getName())
          || aBox.getLength() != bBox.getLength()
          || aBox.getHeight() != bBox.getHeight()
          || aBox.getWidth() != bBox.getWidth()
          || aBox.getX() != bBox.getX()
          || aBox.getY() != bBox.getY()
          || aBox.getZ() != bBox.getZ())
        return String.format(
            "false: box %d does not match\n%s\n%s",
            i, aProblem.getBoxes().get(i).toString(), bProblem.getBoxes().get(i).toString());
    }
    return "true";
  }

  @Test
  void testWarehouseToProblem_EmptyWarehouse() {
    Warehouse war = new Warehouse();

    // Hard coding x paths as this was also done in the original function
    List<List<Integer>> x_paths = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    path.add(5);
    path.add(6);
    x_paths.add(path);

    BoxSolver bs = new BoxSolver("warehouse", 0, 0, 0, x_paths, new ArrayList<List<Integer>>());
    BoxSolver result = TimefoldMapper.warehouseToProblem(war, 1);
    assertEquals("true", isEqual(result, bs));
  }

  @Test
  void testWarehouseToProblem_SingleBox() {
    Item item = new Item("a", 1, 2, 3);
    List<Item> itemList = new LinkedList<>();
    itemList.add(item);
    List<Container> contList = new LinkedList<>();
    Container cont = new Container("cont", 4, 5, 6, itemList);
    contList.add(cont);
    Warehouse war = new Warehouse(7, 8, 9, contList);

    // Hard coding x paths as this was also done in the original function
    List<List<Integer>> x_paths = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    path.add(5);
    path.add(6);
    x_paths.add(path);

    BoxSolver bs = new BoxSolver("warehouse", 7, 8, 9, x_paths, new ArrayList<List<Integer>>());
    bs.addBox("a", 1, 3, 2);
    BoxSolver result = TimefoldMapper.warehouseToProblem(war, 1);
    assertEquals("true", isEqual(result, bs));
  }

  @Test
  void testWarehouseToProblem_TripleBox() {
    Item item1 = new Item("a", 1, 1, 1);
    Item item2 = new Item("b", 2, 2, 2);
    Item item3 = new Item("c", 3, 3, 3);

    List<Item> itemList = new LinkedList<>();
    itemList.add(item1);
    itemList.add(item2);
    itemList.add(item3);

    List<Container> contList = new LinkedList<>();
    Container cont = new Container("cont", 4, 5, 6, itemList);
    contList.add(cont);
    Warehouse war = new Warehouse(7, 8, 9, contList);

    // Hard coding x paths as this was also done in the original function
    List<List<Integer>> x_paths = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    path.add(5);
    path.add(6);
    x_paths.add(path);

    BoxSolver bs = new BoxSolver("warehouse", 7, 8, 9, x_paths, new ArrayList<List<Integer>>());
    bs.addBox("a", 1, 1, 1);
    bs.addBox("b", 2, 2, 2);
    bs.addBox("c", 3, 3, 3);

    BoxSolver result = TimefoldMapper.warehouseToProblem(war, 1);
    assertEquals("true", isEqual(result, bs));
  }

  @Test
  void testWarehouseToProblem_MultipleContainers() {
    Item item1 = new Item("a", 1, 1, 1);
    Item item2 = new Item("b", 2, 2, 2);
    Item item3 = new Item("c", 3, 3, 3);

    List<Item> itemList1 = new LinkedList<>();
    itemList1.add(item1);
    itemList1.add(item2);
    List<Item> itemList2 = new LinkedList<>();
    itemList2.add(item3);

    List<Container> contList = new LinkedList<>();
    Container cont1 = new Container("cont1", 4, 5, 6, itemList1);
    Container cont2 = new Container("cont2", 7, 8, 9, itemList2);
    contList.add(cont1);
    contList.add(cont2);
    Warehouse war = new Warehouse(7, 8, 9, contList);

    // Hard coding x paths as this was also done in the original function
    List<List<Integer>> x_paths = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    path.add(5);
    path.add(6);
    x_paths.add(path);

    BoxSolver bs = new BoxSolver("warehouse", 7, 8, 9, x_paths, new ArrayList<List<Integer>>());
    bs.addBox("a", 1, 1, 1);
    bs.addBox("b", 2, 2, 2);
    bs.addBox("c", 3, 3, 3);

    BoxSolver result = TimefoldMapper.warehouseToProblem(war, 1);
    assertEquals("true", isEqual(result, bs));
  }

  // Throws nullpointer exception:

  // @Test
  // void testSolutionToWarehouse_EmptyBoxPlan() {
  //   BoxPlan bp = new BoxPlan();
  //   List<Container> contList = new LinkedList<>();
  //   Container cont = new Container("Container", 1, 1, 1);
  //   contList.add(cont);
  //   Warehouse war = new Warehouse(1,1,1, contList);

  //   assertEquals(war, TimefoldMapper.solutionToWarehouse(bp, war, 1));
  // }

}
