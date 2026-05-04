package com.xlrit.boxoptimization.model;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = true)
@Setter
public class Warehouse extends Dimensionable {

  private List<Container> containers = new ArrayList<>();
  private List<Item> unplacedItems = new ArrayList<>();

  public Warehouse() {}

  public Warehouse(double length, double height, double width, List<Container> containers) {
    super("warehouse", length, height, width);
    this.containers = containers;
  }

  public void addUnplacedItem(Item item) {
    unplacedItems.add(item);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("Warehouse with dimensions %.2fx%.2fx%.2f\n"
        .formatted(getLength(), getHeight(), getWidth()));
    for (Container c : containers) {
      result.append(c.toString());
    }
    if (!unplacedItems.isEmpty()) {
      result.append("Unplaced items:\n");
      for (Item item : unplacedItems) {
        result.append("  ").append(item.toString()).append("\n");
      }
    }
    return result.toString();
  }
}
