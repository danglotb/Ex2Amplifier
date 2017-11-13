package fr.inria.stamp;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.alloy.builder.ModelBuilder;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.instrumentation.TestInstrumentation;
import fr.inria.stamp.instrumentation.processor.ConstraintInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ExecutableInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.InvocationInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ModificationInstrumenterProcessor;
import fr.inria.stamp.test.launcher.TestLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class MainTest extends AbstractTest {

    @Test
    public void test() {
        try {
            final InputConfiguration configuration = new InputConfiguration("src/test/resources/calculator/calculator.properties");
            final Launcher launcher = init(configuration);
            AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
            String dependencies = builder.buildClasspath(configuration.getInputProgram().getProgramDir());
            final CtClass<Object> testClass = launcher.getFactory().Class().get("fr.inria.calculator.CalculatorTest");
            final List<CtMethod<?>> tests = testClass.getMethodsByName("testAccumulate");
            AmplificationHelper.setTimeOutInMs(100000);
            TestLauncher.runFromSpoonNodes(configuration,
                    "src/test/resources/calculator/target/classes/"
                            + AmplificationHelper.PATH_SEPARATOR + dependencies,
                    testClass, tests
            );
            final Model model = ModelBuilder.getModel();
            assertEquals(expectedAlloyModel, model.toAlloy());
            final Model negatedModel = model.negateNextConstraint();
            assertEquals(expectedNegatedAlloyModel, negatedModel.toAlloy());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String expectedAlloyModel = "one sig InputVector {" + nl +
            "\tinput_0:Int," + nl +
            "\tinput_1:Int" + nl +
            "}" + nl +
            "one sig parameterVector {" + nl +
            "\tparameter_value_int_0:Int," + nl +
            "\tparameter_value_int_1:Int" + nl +
            "}" + nl +
            "abstract sig fr_inria_calculator_Calculator {" + nl +
            "\t\tcurrentValue:Int" + nl +
            "}" + nl +
            "one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}" + nl +
            "one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}" + nl +
            "fact {" + nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_0" + nl +
            "\tfr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0" + nl +
            "\tparameterVector.parameter_value_int_1 = InputVector.input_1" + nl +
            "\tnot rem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0" + nl +
            "\tfr_inria_calculator_Calculator_0_2.currentValue = plus[fr_inria_calculator_Calculator_0_1.currentValue,mul[2,parameterVector.parameter_value_int_1]]" + nl +
            "}" + nl +
            "run {} for 2";

    private static final String expectedNegatedAlloyModel = "one sig InputVector {" + nl +
            "\tinput_0:Int," + nl +
            "\tinput_1:Int" + nl +
            "}" + nl +
            "one sig parameterVector {" + nl +
            "\tparameter_value_int_0:Int," + nl +
            "\tparameter_value_int_1:Int" + nl +
            "}" + nl +
            "abstract sig fr_inria_calculator_Calculator {" + nl +
            "\t\tcurrentValue:Int" + nl +
            "}" + nl +
            "one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}" + nl +
            "one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}" + nl +
            "fact {" + nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_0" + nl +
            "\tfr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0" + nl +
            "\tparameterVector.parameter_value_int_1 = InputVector.input_1" + nl +
            "\trem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0" + nl +
            "}" + nl +
            "run {} for 2";

    private static Launcher init(final InputConfiguration configuration) throws IOException {
        AutomaticBuilderFactory.reset();

        final InputProgram program = InputConfiguration.initInputProgram(configuration);
        program.setProgramDir(DSpotUtils.computeProgramDirectory.apply(configuration));
        configuration.setInputProgram(program);
        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
        String dependencies = builder.buildClasspath(program.getProgramDir());
        if (configuration.getProperty("additionalClasspathElements") != null) {
            dependencies = dependencies + AmplificationHelper.PATH_SEPARATOR + program.getProgramDir() + configuration.getProperty("additionalClasspathElements");
        }
        File output = new File(program.getProgramDir() + "/" + program.getClassesDir());
        File outputTest = new File(program.getProgramDir() + "/" + program.getTestClassesDir());
        try {
            FileUtils.cleanDirectory(output);
            FileUtils.cleanDirectory(outputTest);
        } catch (Exception ignored) {
            //ignored
        }
        Launcher spoonModel = instrument(program.getAbsoluteSourceCodeDir(),
                program.getAbsoluteTestSourceCodeDir(),
                dependencies);
        SpoonModelBuilder modelBuilder = spoonModel.getModelBuilder();
        modelBuilder.setBinaryOutputDirectory(output);
        boolean status = modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
        DSpotUtils.copyResources(configuration);
        if (!status) {
            throw new RuntimeException("Error during compilation");
        }
        return spoonModel;
    }

    private static Launcher instrument(String pathToSources, String pathToTestSources, String dependencies) {
        Launcher launcher = initLauncher(pathToSources, pathToTestSources, dependencies);
        launcher.addProcessor(new InvocationInstrumenterProcessor());
        launcher.addProcessor(new ExecutableInstrumenterProcessor());
        launcher.addProcessor(new ConstraintInstrumenterProcessor());
        launcher.addProcessor(new ModificationInstrumenterProcessor());
        launcher.process();
        launcher.getFactory()
                .Class()
                .getAll()
                .stream()
                .flatMap(ctType -> ctType.getMethods().stream())
                .filter(AmplificationChecker::isTest)
                .forEach(TestInstrumentation::instrument);
        return launcher;
    }

}
