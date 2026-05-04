package com.xlrit.boxoptimization.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class Dimensionable {
  private double length;
  private double width;
  private double height;

  @EqualsAndHashCode.Exclude private String id;

  public Dimensionable() {}

  public Dimensionable(String id, double length, double height, double width) {
    if (length <= 0 || width <= 0 || height <= 0)
      throw new IllegalArgumentException("Object size has to be greater than 0");

    this.id = id;
    this.length = length;
    this.height = height;
    this.width = width;
  }

  public void setLength(double length) {
    if (length <= 0) throw new IllegalArgumentException("Lenght has to be greater than 0");
    this.length = length;
  }

  public void setHeight(double height) {
    if (height <= 0) throw new IllegalArgumentException("Height has to be greater than 0");
    this.height = height;
  }

  public void setWidth(double width) {
    if (width <= 0) throw new IllegalArgumentException("Width has to be greater than 0");
    this.width = width;
  }

  // Returns the volume of the object
  public double getVolume() {
    return length * width * height;
  }

  public Dimensionable(Dimensionable other) {
    this.id = other.getId();
    this.height = other.getHeight();
    this.length = other.getLength();
    this.width = other.getWidth();
  }
}
