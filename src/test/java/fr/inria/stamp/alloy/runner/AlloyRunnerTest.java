package fr.inria.stamp.alloy.runner;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class AlloyRunnerTest {

    @Test
    public void testRun() throws Exception {
        final List<Object> newValues = AlloyRunner.run("src/test/resources/alloy/calculator.als");
        assertEquals(-5, newValues.get(0));
        assertEquals(3, newValues.get(1));
    }

    @Test
    public void testUnsatModel() throws Exception {
        final List<Object> newValues = AlloyRunner.run("src/test/resources/alloy/unsat_model.als");
        assertTrue(newValues.isEmpty());
    }
}
