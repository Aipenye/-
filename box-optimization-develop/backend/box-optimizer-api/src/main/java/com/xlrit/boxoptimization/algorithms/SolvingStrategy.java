package com.xlrit.boxoptimization.algorithms;

import com.xlrit.boxoptimization.model.Warehouse;

public interface SolvingStrategy {
  Warehouse solve(Warehouse warehouse);
}
