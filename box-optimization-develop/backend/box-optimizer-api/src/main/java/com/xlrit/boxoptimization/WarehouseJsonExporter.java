package com.xlrit.boxoptimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlrit.boxoptimization.dto.SolveButtonDTO;
import com.xlrit.boxoptimization.model.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class WarehouseJsonExporter {
  public static void main(String[] args) {
    try {
      // Sample warehouse (you would use your real one here)
      Warehouse warehouse = new Warehouse(10, 20, 30, List.of(new Container("1", 1, 1, 1)));
      warehouse.getContainers().get(0).addBox(new Item("1", 1, 1, 1));

      SolveButtonDTO wrapper = new SolveButtonDTO(warehouse, "timefold");
      ObjectMapper mapper = new ObjectMapper();

      // Serialize to a file
      mapper.writerWithDefaultPrettyPrinter().writeValue(new File("warehouse.json"), wrapper);

      System.out.println("Warehouse JSON written to file.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
