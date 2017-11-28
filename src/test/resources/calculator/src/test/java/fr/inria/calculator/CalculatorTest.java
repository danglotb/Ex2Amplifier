package fr.inria.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/10/17
 */
public class CalculatorTest {

    @Test
    public void testAccumulate() throws Exception {
        final Calculator calculator1 = new Calculator(-5);
        assertEquals(-5, calculator1.getCurrentValue());
        calculator1.accumulate(-5);
        assertEquals(-15, calculator1.getCurrentValue());
    }

    @Test
    public void testDoubleAccumulate() throws Exception {
        final Calculator calculator1 = new Calculator(-5);
        assertEquals(-5, calculator1.getCurrentValue());
        calculator1.accumulate(-5);
        assertEquals(-15, calculator1.getCurrentValue());
        calculator1.accumulate(700);
        assertEquals(685, calculator1.getCurrentValue());
    }

    @Test
    public void testAccumulateAndReset() throws Exception {
        final Calculator calculator1 = new Calculator(3);
        assertEquals(3, calculator1.getCurrentValue());
        calculator1.accumulate(-3);
        assertEquals(0, calculator1.getCurrentValue());
        calculator1.reset();
        assertEquals(0, calculator1.getCurrentValue());
    }

    @Test
    public void testAggregate() throws Exception {
        final Calculator calculator1 = new Calculator(-2);
        assertEquals(-2, calculator1.getCurrentValue());
        final Calculator calculator2 = new Calculator(-4);
        assertEquals(-4, calculator2.getCurrentValue());
        calculator1.aggregate(calculator2);
        assertEquals(0, calculator1.getCurrentValue());
        assertEquals(-24, calculator2.getCurrentValue());
    }

}