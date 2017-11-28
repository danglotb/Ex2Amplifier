package fr.inria.stamp;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class Ex2AmplifierTest extends AbstractTest {

    @Test
    public void test() throws Exception {
        final String pathToConfiguration = "src/test/resources/calculator/calculator.properties";
        final Ex2Amplifier amplifier = new Ex2Amplifier(pathToConfiguration);
        final Factory factory = initSpoonModel(pathToConfiguration);
        final CtClass<Object> testClass = factory.Class().get("fr.inria.calculator.CalculatorTest");
        amplifier.reset(testClass);
        final List<CtMethod> amplifiedTestAccumulate = amplifier.apply(testClass.getMethodsByName("testAccumulate").get(0));
        assertEquals(expectedAmplifiedTestAccumulate, amplifiedTestAccumulate.toString());
    }

    private static final String expectedAmplifiedTestAccumulate = "[@Test" + nl +
            "public void testAccumulate_Ex2_0() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(3);" + nl +
            "    Assert.assertEquals((-5), calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(7);" + nl +
            "    Assert.assertEquals((-15), calculator1.getCurrentValue());" + nl +
            "}]";

    @Test
    public void testAmplifyTestDoubleAccumulate() throws Exception {
        final String pathToConfiguration = "src/test/resources/calculator/calculator.properties";
        final Ex2Amplifier amplifier = new Ex2Amplifier(pathToConfiguration);
        final Factory factory = initSpoonModel(pathToConfiguration);
        final CtClass<Object> testClass = factory.Class().get("fr.inria.calculator.CalculatorTest");
        amplifier.reset(testClass);
        final List<CtMethod> testDoubleAccumulate = amplifier.apply(testClass.getMethodsByName("testDoubleAccumulate").get(0));
        assertEquals(2, testDoubleAccumulate.size());
        assertEquals(expectedAmplifiedTestDoubleAccumulate, testDoubleAccumulate.toString());
    }

    private static final String expectedAmplifiedTestDoubleAccumulate = "[@Test" + nl +
            "public void testDoubleAccumulate_Ex2_0() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(3);" + nl +
            "    Assert.assertEquals((-5), calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(7);" + nl +
            "    Assert.assertEquals((-15), calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(-5);" + nl +
            "    Assert.assertEquals(685, calculator1.getCurrentValue());" + nl +
            "}, @Test" + nl +
            "public void testDoubleAccumulate_Ex2_1() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(-7);" + nl +
            "    Assert.assertEquals((-5), calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(7);" + nl +
            "    Assert.assertEquals((-15), calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(7);" + nl +
            "    Assert.assertEquals(685, calculator1.getCurrentValue());" + nl +
            "}]";

    @Test
    public void testAmplifyTestAccumulateAndReset() throws Exception {
        final String pathToConfiguration = "src/test/resources/calculator/calculator.properties";
        final Ex2Amplifier amplifier = new Ex2Amplifier(pathToConfiguration);
        final Factory factory = initSpoonModel(pathToConfiguration);
        final CtClass<Object> testClass = factory.Class().get("fr.inria.calculator.CalculatorTest");
        amplifier.reset(testClass);
        final List<CtMethod> testDoubleAccumulate = amplifier.apply(testClass.getMethodsByName("testAccumulateAndReset").get(0));
        assertEquals(expectedAmplifiedTestAccumulateAndReset, testDoubleAccumulate.toString());
    }

    private static final String expectedAmplifiedTestAccumulateAndReset = "[@Test" + nl +
            "public void testAccumulateAndReset_Ex2_0() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(-5);" + nl +
            "    Assert.assertEquals(3, calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(6);" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "    calculator1.reset();" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "}, @Test" + nl +
            "public void testAccumulateAndReset_Ex2_1() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(3);" + nl +
            "    Assert.assertEquals(3, calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(4);" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "    calculator1.reset();" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "}, @Test" + nl +
            "public void testAccumulateAndReset_Ex2_2() throws Exception {" + nl +
            "    final Calculator calculator1 = new Calculator(3);" + nl +
            "    Assert.assertEquals(3, calculator1.getCurrentValue());" + nl +
            "    calculator1.accumulate(-8);" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "    calculator1.reset();" + nl +
            "    Assert.assertEquals(0, calculator1.getCurrentValue());" + nl +
            "}]";

    private Factory initSpoonModel(String pathToConfigurationFile) {
        try {
            final InputConfiguration configuration = new InputConfiguration(pathToConfigurationFile);
            final InputProgram program = InputConfiguration.initInputProgram(configuration);
            AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
            String dependencies = builder.buildClasspath(program.getProgramDir());
            return initLauncher(program.getAbsoluteSourceCodeDir(),
                    program.getAbsoluteTestSourceCodeDir(),
                    dependencies).getFactory();
        } catch (IOException ignored) {
            fail();//should not happen
        }
        return null;// make javac happy
    }

}

