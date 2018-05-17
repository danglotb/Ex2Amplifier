package eu.stamp_project.ex2amplifier.jbse;

import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.sosiefier.InputProgram;
import eu.stamp_project.ex2amplifier.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class JBSERunnerTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            Test the run method of JBSERunner. JBSE should return a List of Map,
            that associates a name of parameter to its constraints to reach each state.
            Parameter are extracted literals from test.
         */

        final InputProgram program = this.configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                this.configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtMethod<?> testMethod = this.launcher.getFactory().Class()
                .get("fr.inria.calculator.CalculatorTest")
                .getMethodsByName("testAccumulateWithParameters")
                .get(0);

        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);
        assertEquals(2, conditionOnVariablesForEachState.size());
        assertEquals("[{param1=[param1 % 3 == 0]}, {param1=[param1 % 3 != 0]}]", conditionOnVariablesForEachState.toString());
    }

    @Test
    public void test2() throws Exception {

        /*
            Same as Test, but on another test method (compareTo).
            In this case, there is also a parameter in the second operand
         */

        final InputProgram program = configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtMethod<?> testMethod = this.launcher.getFactory().Class()
                .get("fr.inria.calculator.CalculatorTest")
                .getMethodsByName("testCompareToWithParameters")
                .get(0);
        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);
        assertEquals("[{param1=[param1 > param2]}, {param1=[param1 <= param2]}]", conditionOnVariablesForEachState.toString());
    }
}
