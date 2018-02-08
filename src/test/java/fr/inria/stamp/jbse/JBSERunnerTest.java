package fr.inria.stamp.jbse;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.IOException;
import java.util.Arrays;
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
