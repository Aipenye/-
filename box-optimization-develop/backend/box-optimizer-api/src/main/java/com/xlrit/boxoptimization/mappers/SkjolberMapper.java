package com.xlrit.boxoptimization.mappers;

import com.example.boxpackaging.SkjolberSolver;
import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.BoxStackValue;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.visualizer.api.packaging.ContainerVisualizer;
import com.github.skjolber.packing.visualizer.api.packaging.PackagingResultVisualizer;
import com.github.skjolber.packing.visualizer.api.packaging.StackPlacementVisualizer;
import com.github.skjolber.packing.visualizer.packaging.DefaultPackagingResultVisualizerFactory;
import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.ArrayList;
import java.util.List;

public class SkjolberMapper {
  public static SkjolberSolver warehouseToProblem(Warehouse givenWarehouse) {
    SkjolberSolver skjolberSolver = new SkjolberSolver();
    for (Container container : givenWarehouse.getContainers()) {
      skjolberSolver.addContainer(
          container.getId(),
          getUnits(container.getLength(), 1),
          getUnits(container.getWidth(), 1),
          getUnits(container.getHeight(), 1));
      for (Item item : container.getItems()) {
        skjolberSolver.addBox(
            item.getId(),
            getUnits(item.getLength(), 1),
            getUnits(item.getWidth(), 1),
            getUnits(item.getHeight(), 1));
      }
    }
    return skjolberSolver;
  }

  public static Warehouse solutionToWarehouse(
      PackagerResult solution, Warehouse warehouse, List<StackableItem> unplacedItems) {
    // open solution
    DefaultPackagingResultVisualizerFactory factory = new DefaultPackagingResultVisualizerFactory();
    PackagingResultVisualizer visualSolution = factory.visualize(solution.getContainers());

    List<Container> newContainers = new ArrayList<>();
    for (ContainerVisualizer container : visualSolution.getContainers()) {
      System.out.println(container);
      Container newContainer =
          new Container(container.getId(), container.getDx(), container.getDy(), container.getDz());

      for (StackPlacementVisualizer stackPlacement : container.getStack().getPlacements()) {
        Item item =
            new Item(
                stackPlacement.getStackable().getId(),
                stackPlacement.getStackable().getDx(),
                stackPlacement.getStackable().getDz(),
                stackPlacement.getStackable().getDy(),
                stackPlacement.getX(),
                stackPlacement.getZ(),
                stackPlacement.getY());

        newContainer.addBox(item);
      }
      newContainers.add(newContainer);
    }
    Warehouse newWarehouse =
        new Warehouse(
            getUnits(warehouse.getLength(), 1),
            getUnits(warehouse.getWidth(), 1),
            getUnits(warehouse.getHeight(), 1),
            newContainers);
    List<Item> unplaced = new ArrayList<Item>();
    for (StackableItem item : unplacedItems) {
      Box box = (Box) item.getStackable();
      BoxStackValue[] rotations = box.getStackValues();

      if (rotations.length > 0) {
        BoxStackValue orientation = rotations[0];
        int length = orientation.getDx();
        int height = orientation.getDz();
        int width = orientation.getDy();

        unplaced.add(new Item("unplaced", length, width, height));
      }
    }

    // Returns
    newWarehouse.setUnplacedItems(unplaced);
    return newWarehouse;
  }

  private static int getUnits(double dimension, double unitSize) {
    return (int) Math.ceil(dimension / unitSize);
  }
}
