package eu.stamp.project.ex2amplifier.smt;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/01/18
 */
public class SMTSolverTest {

    @Test
    @Ignore
    public void testSolve2() throws Exception {
        Map<String, List<String>> constraintsPerParamName = new HashMap<>();
        constraintsPerParamName.put("param1", new ArrayList<>());
        constraintsPerParamName.put("param2", new ArrayList<>());
        constraintsPerParamName.get("param1").add("param1 % 3 == 0");
        constraintsPerParamName.get("param2").add("param1 * param1 == param2");
        final List<?> solutions = SMTSolver.solve(constraintsPerParamName);
        assertEquals(2, solutions.size());
        //assertEquals(6, values.get(0).getValue());// TODO must support types conversion
    }

    @Test
    public void testSolve() throws Exception {

        /*
            The SMTSolver should be able to find a model, and more value for
            the given problem.
            The problem is encapsulate into a Map<String, List<String>>, which is
            the output of the JBSERunner.
            The key of the map is the name of the parameter, and it is associated to
            a list of constraint.
            The output of the solve method is a List<CtLiteral<?>> which are the list
            of values that each parameter has to take to trigger a specific path,
            previsously found by JBSE.
         */

        Map<String, List<String>> constraintsPerParamName = new HashMap<>();
        constraintsPerParamName.put("param1", new ArrayList<>());
        constraintsPerParamName.put("param2", new ArrayList<>());
        constraintsPerParamName.get("param1").add("param1 % 3 == 0");
        constraintsPerParamName.get("param1").add("param1 > 3");
        constraintsPerParamName.get("param1").add("param1 >= 3");
        constraintsPerParamName.get("param1").add("param1 - 3 == param2");
        constraintsPerParamName.get("param2").add("param2 <= 3");
        constraintsPerParamName.get("param2").add("param2 < 3");
        constraintsPerParamName.get("param2").add("param2 + 1 != 0");
        constraintsPerParamName.get("param2").add("param2 * 2 == param1");
        constraintsPerParamName.get("param2").add("param2 / 2 > 0 && param2 / 4 > 0");
        final List<?> solutions = SMTSolver.solve(constraintsPerParamName);
        assertEquals(2, solutions.size());
        //assertEquals(6, values.get(0).getValue());// TODO must support types conversion
    }

    @Test
    public void testSolveThrowUnsat() throws Exception {
        Map<String, List<String>> constraintsPerParamName = new HashMap<>();
        constraintsPerParamName.put("param1", new ArrayList<>());
        constraintsPerParamName.get("param1").add("param1 == 2");
        constraintsPerParamName.get("param1").add("param1 == 3");
        assertTrue(SMTSolver.solve(constraintsPerParamName).isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testSolveReturnNull() throws Exception {
        Map<String, List<String>> constraintsPerParamName = new HashMap<>();
        constraintsPerParamName.put("param1", new ArrayList<>());
        constraintsPerParamName.get("param1").add("param1");
        assertNull(SMTSolver.solve(constraintsPerParamName));
    }

    @Test
    public void testWithParenthesis() throws Exception {
        Map<String, List<String>> constraintsPerParamName = new HashMap<>();
        constraintsPerParamName.put("param1", new ArrayList<>());
        constraintsPerParamName.get("param1").add("param1 - ((param1 * 52429 >>> 19 << 3) + (param1 * 52429 >>> 19 << 1)) == 0");
        constraintsPerParamName.get("param1").add("param1 * 52429 >>> 19 == 0");
        final List<?> solutions = SMTSolver.solve(constraintsPerParamName);
        assertEquals(1, solutions.size());
    }
}
