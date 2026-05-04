package com.xlrit.boxoptimization.model;

public interface Coordinateable {

  // Getter and Setter for X coordinate
  double getX();

  void setX(double x);

  // Getter and Setter for Y coordinate
  // !NOTE: is this the height coordinate?
  double getY();

  void setY(double y);

  // Getter and Setter for Z coordinate
  double getZ();

  void setZ(double z);

  // TODO: Implement or remove unused distance function:
  // // Calculates distance between two coordinatable objects
  // static double distanceTo(Coordinateable other){
  //     throw new UnsupportedOperationException("Unimplemented method 'distanceTo'");
  // }
}
