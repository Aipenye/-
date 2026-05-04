package com.xlrit.boxoptimization.model;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Container extends Dimensionable {

  private List<Item> items = new ArrayList<>();

  public Container() {}

  public Container(String id, double length, double width, double height) {
    super(id, length, width, height);
  }

  public Container(String id, double length, double width, double height, List<Item> boxes) {
    super(id, length, width, height);
    this.items = boxes;
  }

  public Container(Container other) {
    super(other);
    this.items = other.items;
  }

  public void addBox(Item item) {
    this.items.add(item);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(
        String.format("Container %s (%.2fx%.2fx%.2f)\nWith items:\n",
            getId(), getLength(), getHeight(), getWidth()));
    for (Item item : items) {
      result.append("  ").append(item).append("\n");
    }
    return result.toString();
  }
}
