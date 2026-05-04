package com.xlrit.boxoptimization.algorithms;

import com.xlrit.boxoptimization.mappers.TimefoldMapper;
import com.xlrit.boxoptimization.model.Warehouse;
import org.acme.timefold.domain.BoxPlan;
import org.acme.timefold.solver.BoxSolver;
import org.springframework.stereotype.Component;

@Component("timefold")
public class TimefoldStrategy implements SolvingStrategy {

  @Override
  public Warehouse solve(Warehouse warehouse) {
    double unitSize = TimefoldMapper.calculateUnits(warehouse);
    BoxSolver timeFoldProblem = TimefoldMapper.warehouseToProblem(warehouse, unitSize);
    BoxPlan solution = timeFoldProblem.solve();
    return TimefoldMapper.solutionToWarehouse(solution, warehouse, unitSize);
  }
}
