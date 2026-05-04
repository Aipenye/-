package com.xlrit.boxoptimization.dto;

import com.xlrit.boxoptimization.model.Warehouse;

public class SolveButtonDTO {

  private Warehouse warehouse;
  private String solvingStrategy;

  public SolveButtonDTO() {}

  public SolveButtonDTO(Warehouse warehouse, String solvingStrategy) {
    this.warehouse = warehouse;
    this.solvingStrategy = solvingStrategy;
  }

  public Warehouse getWarehouse() {
    return warehouse;
  }

  public String getSolvingStrategy() {
    return solvingStrategy;
  }
}
