package com.xlrit.boxoptimization.testutil;

import com.github.skjolber.packing.api.Container;

public class SkjolbergContainerWrapper {
  Container container;

  SkjolbergContainerWrapper(Container container) {
    this.container = container;
  }

  Container getContainer() {
    return this.container;
  }
}
