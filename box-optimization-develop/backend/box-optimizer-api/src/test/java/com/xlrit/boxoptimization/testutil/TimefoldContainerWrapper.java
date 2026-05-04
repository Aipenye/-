package com.xlrit.boxoptimization.testutil;

import java.util.List;
import org.acme.timefold.domain.Container;

public class TimefoldContainerWrapper {
  Container container;

  public TimefoldContainerWrapper(Container container) {
    this.container = container;
  }

  public TimefoldContainerWrapper(
      String id,
      String name,
      int xbound,
      int ybound,
      int zbound,
      List<List<Integer>> x_paths,
      List<List<Integer>> z_paths) {
    this.container = new Container(id, name, xbound, ybound, zbound, x_paths, z_paths);
  }

  public Container getContainer() {
    return container;
  }
}
