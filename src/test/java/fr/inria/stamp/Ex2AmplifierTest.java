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
        System.out.println(amplifiedTestAccumulate);
        final List<CtMethod> amplifiedTestAccumulateAndReset = amplifier.apply(testClass.getMethodsByName("testAccumulateAndReset").get(0));
        System.out.println(amplifiedTestAccumulateAndReset);
    }

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

