package com.xlrit.boxoptimization.mappers;

import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import org.acme.timefold.domain.Box;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.solver.BoxSolver;

public class TimefoldMapper {

  private static int maxWarehouseVolume = 40 * 40 * 40;

  public static BoxSolver warehouseToProblem(Warehouse givenWarehouse, double unitSize) {
    List<List<Integer>> x_paths = new ArrayList<>();
    List<List<Integer>> z_paths = new ArrayList<>();
    BoxSolver solver = new BoxSolver(
        "warehouse",
        getWarehouseUnits(givenWarehouse.getLength(), unitSize),
        getWarehouseUnits(givenWarehouse.getHeight(), unitSize),
        getWarehouseUnits(givenWarehouse.getWidth(), unitSize),
        x_paths,
        z_paths);

    for (Container container : givenWarehouse.getContainers()) {
      for (Item item : container.getItems()) {
        if (item.isPlaced()) {
          solver.addLockedBox(
              item.getId(),
              getUnits(item.getLength(), unitSize),
              getUnits(item.getHeight(), unitSize),
              getUnits(item.getWidth(), unitSize),
              getUnits(item.getX(), unitSize),
              getUnits(item.getY(), unitSize),
              getUnits(item.getZ(), unitSize));
        } else {
          solver.addBox(
              item.getId(),
              getUnits(item.getLength(), unitSize),
              getUnits(item.getHeight(), unitSize),
              getUnits(item.getWidth(), unitSize));
        }
      }
    }
    return solver;
  }

  public static double calculateUnits(Warehouse warehouse) {
    double[] itemSizes = Stream.concat(
            warehouse.getContainers().stream()
                .flatMap(container -> container.getItems().stream()),
            warehouse.getUnplacedItems().stream())
        .flatMapToDouble(
            item -> DoubleStream.of(item.getLength(), item.getHeight(), item.getWidth()))
        .distinct()
        .toArray();
    double bestUnit = 1;
    double scoreBestUnit = Double.MAX_VALUE;

    for (double itemSize : itemSizes) {
      for (int i = 1; i < 6; i++) {
        double unitSize = itemSize / i;
        if (getUnits(warehouse.getVolume(), Math.pow(unitSize, 3)) > maxWarehouseVolume) {
          continue;
        }
        double score = scoreUnit(warehouse, unitSize);
        if (score < scoreBestUnit) {
          bestUnit = unitSize;
          scoreBestUnit = score;
        }
      }
    }
    return bestUnit;
  }

  private static double scoreUnit(Warehouse warehouse, double unitSize) {
    double deviation = 0;
    int quantity = 0;
    for (Container container : warehouse.getContainers()) {
      for (Item item : container.getItems()) {
        deviation += ((getUnits(item.getLength(), unitSize) * unitSize)
                * (getUnits(item.getHeight(), unitSize) * unitSize)
                * (getUnits(item.getWidth(), unitSize) * unitSize))
            - item.getLength() * item.getHeight() * item.getWidth();
        quantity += 1;
      }
    }
    for (Item item : warehouse.getUnplacedItems()) {
      deviation += ((getUnits(item.getLength(), unitSize) * unitSize)
              * (getUnits(item.getHeight(), unitSize) * unitSize)
              * (getUnits(item.getWidth(), unitSize) * unitSize))
          - item.getLength() * item.getHeight() * item.getWidth();
      quantity += 1;
    }
    deviation += warehouse.getLength() * warehouse.getHeight() * warehouse.getWidth()
        - (getWarehouseUnits(warehouse.getLength(), unitSize) * unitSize
            * getWarehouseUnits(warehouse.getHeight(), unitSize) * unitSize
            * getWarehouseUnits(warehouse.getWidth(), unitSize) * unitSize);
    quantity += 1;
    return deviation / (3 * quantity);
  }

  public static Warehouse solutionToWarehouse(BoxPlan solution, Warehouse warehouse, double unitSize) {
    List<Box> boxes = solution.getBoxes();
    List<Container> newContainers = new ArrayList<>();
    for (Container container : warehouse.getContainers()) {
      newContainers.add(new Container(
          container.getId(), container.getLength(), container.getWidth(), container.getHeight()));
    }

    Container mainContainer = newContainers.get(0);
    List<Item> unplacedItems = new ArrayList<>();
    for (Box box : boxes) {
      Item oldItem = findItem(warehouse, box.getId());
      Item item = new Item(oldItem.getId(), oldItem.getLength(), oldItem.getHeight(), oldItem.getWidth());
      if (box.getAirCube() == null) {
        unplacedItems.add(item);
      } else {
        item.setX(box.getX() * unitSize);
        item.setY(box.getY() * unitSize);
        item.setZ(box.getZ() * unitSize);
        mainContainer.addBox(item);
      }
    }

    Warehouse newWarehouse = new Warehouse(
        warehouse.getLength(), warehouse.getHeight(), warehouse.getWidth(), newContainers);
    newWarehouse.setUnplacedItems(unplacedItems);
    return newWarehouse;
  }

  public static Item findItem(Warehouse warehouse, String id) {
    return Stream.concat(
            warehouse.getContainers().stream().flatMap(container -> container.getItems().stream()),
            warehouse.getUnplacedItems().stream())
        .filter(item -> item.getId() == id)
        .collect(Collectors.toList())
        .getFirst();
  }

  private static int getUnits(double dimension, double unitSize) {
    return (int) Math.ceil(dimension / unitSize);
  }

  private static int getWarehouseUnits(double dimension, double unitSize) {
    return (int) Math.floor(dimension / unitSize);
  }
}
