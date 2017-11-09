package fr.inria.calculator;

import org.junit.Test;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/10/17
 */
public class CalculatorTest {

    @Test
    public void testCFG1() throws Exception {
        final Calculator calculator1 = new Calculator(7);
        calculator1.accumulate(512);
    }

    @Test
    public void testAccumulate() throws Exception {
        final Calculator calculator1 = new Calculator(3);
        calculator1.accumulate(-3);
        calculator1.reset();
    }

    @Test
    public void testAggregate() throws Exception {
        final Calculator calculator1 = new Calculator(-2);
        final Calculator calculator2 = new Calculator(-4);
        calculator1.aggregate(calculator2);
    }
}