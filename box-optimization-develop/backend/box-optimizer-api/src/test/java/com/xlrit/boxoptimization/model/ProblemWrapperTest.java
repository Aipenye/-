// package com.xlrit.boxoptimization.model;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import org.junit.jupiter.api.Test;

// import com.xlrit.boxoptimization.model.ProblemWrapper.Strategy;

// public class ProblemWrapperTest {
// @Test
// void testGetWarehouse() {
// Warehouse war = new Warehouse();
// Strategy strategy = Strategy.TIMEFOLD;

// ProblemWrapper ww = new ProblemWrapper(war, strategy);
// assertEquals(war, ww.getWarehouse());
// }

// @Test
// void testGetStrategy() {
// Warehouse war = new Warehouse();
// Strategy strategy = Strategy.TIMEFOLD;

// ProblemWrapper ww = new ProblemWrapper(war, strategy);
// assertEquals(strategy, ww.getStrategy());

// Warehouse war2 = new Warehouse();
// Strategy strategy2 = Strategy.SKJOLBER;

// ProblemWrapper ww2 = new ProblemWrapper(war2, strategy2);
// assertEquals(strategy2, ww2.getStrategy());
// }

// @Test
// void testSetWarehouse() {
// Warehouse war = new Warehouse();
// ProblemWrapper ww = new ProblemWrapper();
// ww.setWarehouse(war);
// assertEquals(war, ww.getWarehouse());
// }
// }
