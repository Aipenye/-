package com.xlrit.boxoptimization.controller;

import com.xlrit.boxoptimization.dto.SolveButtonDTO;
import com.xlrit.boxoptimization.model.Warehouse;
import com.xlrit.boxoptimization.services.ItemOptimizerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Make sure this port matches the port in of the react webapp
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/algorithm")
public class OptimizerController {

  private final ItemOptimizerService optimizer;

  public OptimizerController(ItemOptimizerService optimizer) {
    this.optimizer = optimizer;
  }

  @PostMapping("/run") // Full path = /api/algorithm/run
  public ResponseEntity<SolveButtonDTO> runAlgorithm(@RequestBody SolveButtonDTO problem) {

    Warehouse toSolve = problem.getWarehouse();
    String solvingStrategy = problem.getSolvingStrategy();
    Warehouse result = this.optimizer.runYourAlgorithm(toSolve, solvingStrategy);

    SolveButtonDTO response = new SolveButtonDTO(result, solvingStrategy);

    return ResponseEntity.ok(response);
  }
}
