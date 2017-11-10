package fr.inria.stamp.instrumentation;

import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/17
 */
public class TestInstrumentationTest extends AbstractTest {

    @Test
    public void testInstrument() throws Exception {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource("src/test/resources/calculator/src/test/java/fr/inria/calculator/CalculatorTest.java");
        launcher.addInputResource("src/main/java/fr/inria/stamp/alloy/");
        launcher.buildModel();
        final CtClass<?> classToBeInstrumented = launcher.getFactory().Class().get("fr.inria.calculator.CalculatorTest");
        final CtMethod<?> testAccumulate = classToBeInstrumented.getMethodsByName("testAccumulate").get(0);
        TestInstrumentation.instrument(testAccumulate);
        assertEquals("test fr.inria.stamp.instrumentation failed", expectedInstrumentedMethod, testAccumulate.toString());
    }

    private static final String expectedInstrumentedMethod = "@Test" + nl +
            "public void testAccumulate() throws Exception {" + nl +
            "    ModelBuilder.addInputs(new fr.inria.stamp.alloy.model.Variable(\"InputVector.input_0\", \"Int\"), new fr.inria.stamp.alloy.model.Variable(\"InputVector.input_1\", \"Int\"));" + nl +
            "    ModelBuilder.addParameters(new fr.inria.stamp.alloy.model.Variable(\"InputVector.input_0\", \"Int\"));" + nl +
            "    final Calculator calculator1 = new Calculator(3);" + nl +
            "    ModelBuilder.addParameters(new fr.inria.stamp.alloy.model.Variable(\"InputVector.input_1\", \"Int\"));" + nl +
            "    calculator1.accumulate((-3));" + nl +
            "    calculator1.reset();" + nl +
            "}";
}
