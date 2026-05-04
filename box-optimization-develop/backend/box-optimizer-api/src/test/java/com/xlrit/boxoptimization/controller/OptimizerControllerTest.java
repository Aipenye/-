package com.xlrit.boxoptimization.controller;

import com.xlrit.boxoptimization.services.ItemOptimizerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OptimizerController.class)
public class OptimizerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ItemOptimizerService optimizer;

  @Test
  public void testSkjolberReturnCorrectSolution1() throws Exception {

    // Warehouse warehouse;
    // SolveButtonDTO solveButtonDTO = new SolveButtonDTO(warehouse, "skjolber");
  }
}
