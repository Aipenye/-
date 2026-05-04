package com.xlrit.boxoptimization.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item extends Dimensionable implements Coordinateable {

  private boolean placed = false;
  private double x;
  private double y;
  private double z;

  public Item() {}

  public Item(String id, double length, double width, double height) {
    super(id, length, width, height);
    this.placed = false;
  }

  public Item(String id, double length, double width, double height, double x, double y, double z) {
    super(id, length, width, height);
    this.x = x;
    this.y = y;
    this.z = z;
    this.placed = true;
  }

  public Item(Item other) {
    super(other);
    this.placed = other.placed;
    this.x = other.x;
    this.y = other.y;
    this.z = other.z;
  }

  public void place(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.placed = true;
  }

  @Override
  public String toString() {
    return placed
        ? String.format(
            "Item (%s)"
                + "  Length: %.2f Height: %.2f Width: %.2f \n"
                + "                                 X: %.2f Y: %.2f Z: %.2f\n",
            getId(), getLength(), getHeight(), getWidth(), getX(), getY(), getZ())
        : String.format(
            "Item (%s) {\n"
                + "  Length: %.2f\n"
                + "  Height: %.2f\n"
                + "  Width: %.2f\n"
                + "  Placement: not placed\n"
                + "}",
            getId(), getLength(), getHeight(), getWidth());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Item other = (Item) o;

    // Compare dimensions as unordered sets
    Set<Double> dims1 = Set.of(getLength(), getHeight(), getWidth());
    Set<Double> dims2 = Set.of(other.getLength(), other.getHeight(), other.getWidth());

    if (!dims1.equals(dims2)) return false;

    // Compare placement state and coordinates
    if (this.placed != other.placed) return false;
    if (placed) {
      return Double.compare(x, other.x) == 0
          && Double.compare(y, other.y) == 0
          && Double.compare(z, other.z) == 0;
    }

    // Both not placed → dimensions were enough
    return true;
  }

  @Override
  public int hashCode() {
    List<Double> dims = Arrays.asList(getLength(), getHeight(), getWidth());
    Collections.sort(dims); // Sort for consistent hashing

    return Objects.hash(
        dims.get(0),
        dims.get(1),
        dims.get(2),
        placed,
        placed ? x : null,
        placed ? y : null,
        placed ? z : null);
  }
}
