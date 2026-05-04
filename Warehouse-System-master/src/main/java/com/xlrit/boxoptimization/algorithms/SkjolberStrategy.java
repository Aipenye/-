package com.xlrit.boxoptimization.algorithms;

import com.example.boxpackaging.SkjolberSolver;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackableItem;
import com.xlrit.boxoptimization.mappers.SkjolberMapper;
import com.xlrit.boxoptimization.model.Warehouse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("skjolber")
public class SkjolberStrategy implements SolvingStrategy {

  @Override
  public Warehouse solve(Warehouse warehouse) {
    SkjolberSolver skjolberSolver = SkjolberMapper.warehouseToProblem(warehouse);
    PackagerResult solution = skjolberSolver.solve();
    List<StackableItem> unplacedItems = skjolberSolver.getUnplacedItems();
    return SkjolberMapper.solutionToWarehouse(solution, warehouse, unplacedItems);
  }
}
