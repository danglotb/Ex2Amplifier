package fr.inria.stamp.alloy.runner;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class AlloyRunnerTest {

    @Test
    public void testRun() throws Exception {
        final List<Object> newValues = AlloyRunner.run("src/test/resources/alloy/calculator.als");
        assertEquals("-5", newValues.get(0));
        assertEquals("-5", newValues.get(1));
    }

}
