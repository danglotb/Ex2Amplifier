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
            final List<CtMethod<?>> tests = testClass.getMethodsByName("testCFG1");
            TestLauncher.runFromSpoonNodes(configuration,
                    "src/test/resources/calculator/target/classes/"
                            + AmplificationHelper.PATH_SEPARATOR + dependencies,
                    testClass, tests
            );
            final Model model = ModelBuilder.getModel();
            assertEquals(expectedAlloyModel, model.toAlloy());
            assertEquals(expectedNegatedAlloyModel, model.negateNextConstraint().toAlloy());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String expectedAlloyModel = "one sig InputVector {" + AbstractTest.nl +
            "\tinput_4:Int," + AbstractTest.nl +
            "\tinput_5:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "one sig parameterVector {" + AbstractTest.nl +
            "\tparameter_value_int_0:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "abstract sig fr_inria_calculator_Calculator" + AbstractTest.nl +
            "\t\tcurrentValue:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}" + AbstractTest.nl +
            "one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}" + AbstractTest.nl +
            "fact {" + AbstractTest.nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_4" + AbstractTest.nl +
            "\tfr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0" + AbstractTest.nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_5" + AbstractTest.nl +
            "\tnot rem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0" + AbstractTest.nl +
            "\tfr_inria_calculator_Calculator_0_2.currentValue = plus[fr_inria_calculator_Calculator_0_1.currentValue,mul[2,parameterVector.parameter_value_int_0]]" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "run {} for 2";

    private static final String expectedNegatedAlloyModel = "one sig InputVector {" + AbstractTest.nl +
            "\tinput_4:Int," + AbstractTest.nl +
            "\tinput_5:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "one sig parameterVector {" + AbstractTest.nl +
            "\tparameter_value_int_0:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "abstract sig fr_inria_calculator_Calculator" + AbstractTest.nl +
            "\t\tcurrentValue:Int" + AbstractTest.nl +
            "}" + AbstractTest.nl +
            "one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}" + AbstractTest.nl +
            "one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}" + AbstractTest.nl +
            "fact {" + AbstractTest.nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_4" + AbstractTest.nl +
            "\tfr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0" + AbstractTest.nl +
            "\tparameterVector.parameter_value_int_0 = InputVector.input_5" + AbstractTest.nl +
            "\trem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0" + AbstractTest.nl +
            "}" + AbstractTest.nl +
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
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = (pathToSources + AmplificationHelper.PATH_SEPARATOR + pathToTestSources +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/model/" +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/builder/"
        ).split(AmplificationHelper.PATH_SEPARATOR);
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        if (!dependencies.isEmpty()) {
            String[] dependenciesArray = dependencies.split(AmplificationHelper.PATH_SEPARATOR);
            launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        }
        launcher.buildModel();
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
