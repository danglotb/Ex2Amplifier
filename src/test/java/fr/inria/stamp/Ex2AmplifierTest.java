package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/01/18
 */
public class Ex2AmplifierTest extends AbstractTest {

    // TODO refactor tests

    @Test
    public void test() throws Exception {
        this.configuration.getInputProgram().setFactory(this.launcher.getFactory());
        final Ex2Amplifier amplifier = new Ex2Amplifier(this.configuration);
        final CtClass<?> testClass = this.launcher.getFactory().Class().get("fr.inria.calculator.CalculatorTest");
        amplifier.reset(testClass);
        final List<CtMethod> amplifiedTestAccumulate = amplifier.apply(testClass.getMethodsByName("testAccumulate").get(0));
        assertEquals(2, amplifiedTestAccumulate.size());
        final String expectedAmplifiedTestMethod = "{" + AmplificationHelper.LINE_SEPARATOR +
                "    final Calculator calculator1 = new Calculator(0);" + AmplificationHelper.LINE_SEPARATOR +
                "    Assert.assertEquals((-5), calculator1.getCurrentValue());" + AmplificationHelper.LINE_SEPARATOR +
                "    calculator1.accumulate((-5));" + AmplificationHelper.LINE_SEPARATOR +
                "    Assert.assertEquals((-15), calculator1.getCurrentValue());" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedAmplifiedTestMethod, amplifiedTestAccumulate.get(0).getBody().toString());

    }


}
