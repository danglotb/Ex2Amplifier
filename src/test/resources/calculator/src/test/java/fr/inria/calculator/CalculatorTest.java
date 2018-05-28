package fr.inria.calculator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/10/17
 */
public class CalculatorTest {

    @Before
    public void setUp() throws Exception {
        // empty
    }

    @After
    public void tearDown() throws Exception {
        // empty
    }

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

    public void testAccumulateWithParameters(int param1, int param2) {
        final Calculator calculator1 = new Calculator(param1);
        calculator1.accumulate(param2);
    }

    public void testCompareToWithParameters(int param1, int param2) {
        final Calculator calculator1 = new Calculator(param1);
        calculator1.compareTo(param2);
    }

    @Test
    public void testWithBinaryOnLiteral() throws Exception {
        int zz = 4 + 5;
        boolean z = (zz == 3 || zz < 1);
        assertEquals(9, zz);
    }
}