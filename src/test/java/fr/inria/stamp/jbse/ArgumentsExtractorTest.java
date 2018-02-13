package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.jbse.ArgumentsExtractor;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class ArgumentsExtractorTest {

    @Test
    public void testPerformExtraction() throws Exception {

        /*
            The ArgumentsExtractor should produce a new CtMethod<?>,
            that is semantically equivalent to the test given as input,
            but without assertion, and all used (and supported by JBSE)
            values (literals) will be pass as argument.
            This new CtMethod<?> is used as input for JBSE, and the symbolic execution.
         */

        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.addInputResource("src/test/resources/calculator/src/test/java/fr/inria/calculator/CalculatorTest.java");
        launcher.addInputResource("src/test/resources/calculator/src/main/java/fr/inria/calculator/Calculator.java");
        launcher.buildModel();

        final CtMethod<?> extractedMethod =
                ArgumentsExtractor.performExtraction(launcher.getFactory()
                        .Class()
                        .get("fr.inria.calculator.CalculatorTest")
                        .getMethodsByName("testAccumulate")
                        .get(0)
                );

        assertEquals(expectedMethod, extractedMethod.toString());
    }

    private static final String expectedMethod = "public void extract_testAccumulate(int lit0, int lit1) {" + AmplificationHelper.LINE_SEPARATOR +
            "    final Calculator calculator1 = new Calculator(lit0);" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.getCurrentValue();" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.accumulate(lit1);" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.getCurrentValue();" + AmplificationHelper.LINE_SEPARATOR +
            "}";
}
