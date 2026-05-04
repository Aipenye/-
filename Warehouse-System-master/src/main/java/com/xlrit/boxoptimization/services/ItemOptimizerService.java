package com.xlrit.boxoptimization.services;

import com.xlrit.boxoptimization.algorithms.SolvingStrategy;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ItemOptimizerService {

  private final Map<String, SolvingStrategy> strategyMap;

  public ItemOptimizerService(Map<String, SolvingStrategy> strategyMap) {
    this.strategyMap = strategyMap;
  }

  public Warehouse runYourAlgorithm(Warehouse problem, String strategyType) {
    SolvingStrategy strategy = strategyMap.get(strategyType);
    if (strategy == null) {
      throw new IllegalArgumentException("Unknown strategy type: " + strategyType);
    }
    return strategy.solve(problem);
  }
}
