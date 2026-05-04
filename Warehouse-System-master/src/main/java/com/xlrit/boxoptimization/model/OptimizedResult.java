package com.xlrit.boxoptimization.model;

public class OptimizedResult {

  private Warehouse warehouse;
  private String status;

  public OptimizedResult(Warehouse warehouse, String status) {
    this.warehouse = warehouse;
    this.status = status;
  }

  public Warehouse getWarehouse() { return warehouse; }
  public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
