package fr.inria.stamp;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.alloy.builder.ModelBuilder;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.alloy.runner.AlloyRunner;
import fr.inria.stamp.instrumentation.TestInstrumentation;
import fr.inria.stamp.instrumentation.processor.ConstraintInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ExecutableInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.InvocationInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ModificationInstrumenterProcessor;
import fr.inria.stamp.test.launcher.TestLauncher;
import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class Ex2Amplifier implements Amplifier {

    private static final String pathToOutput = "target/dspot/ex2amplifier/";

    private Launcher spoonModel;

    private InputConfiguration configuration;

    private CtType<?> testClass;

    public Ex2Amplifier(String pathToConfiguration) {
        try {
            this.configuration = new InputConfiguration(pathToConfiguration);
            init(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod> apply(CtMethod testMethod) {
        ModelBuilder.model = new Model();
        List<CtMethod> amplifiedMethods = new ArrayList<>();
        final CtMethod<?> cloneToBeInstrumented = testMethod.clone();
        testMethod.getParent(CtClass.class).addMethod(cloneToBeInstrumented);
        this.instrumentAndCompileTest(cloneToBeInstrumented);
        String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
                .buildClasspath(configuration.getInputProgram().getProgramDir());
        TestLauncher.runFromSpoonNodes(configuration,
                        pathToOutput + AmplificationHelper.PATH_SEPARATOR + dependencies,
                testClass, Collections.singletonList(cloneToBeInstrumented)
        );
        final Model model = ModelBuilder.getModel();
        int counter = 0;
        while (model.hasNextConstraintToBeNegated()) {
            final CtMethod<?> clone = testMethod.clone();
            clone.setSimpleName(testMethod.getSimpleName() + "_Ex2_" + counter++);
            final CtMethod<?> amplifiedMethod = printAndRun(model.negateNextConstraint().toAlloy(), clone);
            if (amplifiedMethod != null) {
                amplifiedMethods.add(amplifiedMethod);
            }
        }
        return AmplificationHelper.updateAmpTestToParent(amplifiedMethods, testMethod);
    }

    private CtMethod<?> printAndRun(String alloyModel, CtMethod<?> testMethod) {
        final File directoryDSpot = new File("target/dspot");
        if (! directoryDSpot.exists()) {
            try {
                FileUtils.forceMkdir(directoryDSpot);
            } catch (IOException ignored) {
                //ignored
            }
        }
        try (FileWriter writer = new FileWriter(new File("target/dspot/model.als"), false)) {
            writer.write(alloyModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<Object> newValues = AlloyRunner.run("target/dspot/model.als");
        if (newValues.isEmpty()) {
            return null;
        }
        testMethod.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
            @Override
            public boolean matches(CtLiteral element) {
                return !AmplificationChecker.isAssert(element.getParent(CtInvocation.class));
            }
        }).forEach(ctLiteral -> {
            if (ctLiteral.getParent() instanceof CtUnaryOperator) {
                ctLiteral.getParent().replace(ctLiteral.getFactory().createLiteral(newValues.remove(0)));
            } else {
                ctLiteral.replace(ctLiteral.getFactory().createLiteral(newValues.remove(0)));
            }
        });
        return testMethod;
    }

    @Override
    public CtMethod applyRandom(CtMethod ctMethod) {
        return null;
    }

    @Override
    public void reset(CtType ctType) {
        this.testClass = ctType;
    }

    private void init(final InputConfiguration configuration) throws IOException {
        AutomaticBuilderFactory.reset();
        final InputProgram program = InputConfiguration.initInputProgram(configuration);
        program.setProgramDir(DSpotUtils.computeProgramDirectory.apply(configuration));
        configuration.setInputProgram(program);
        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
        String dependencies = builder.buildClasspath(program.getProgramDir());
        if (configuration.getProperty("additionalClasspathElements") != null) {
            dependencies = dependencies + AmplificationHelper.PATH_SEPARATOR + program.getProgramDir() + configuration.getProperty("additionalClasspathElements");
        }
        File output = new File(pathToOutput);
        try {
            FileUtils.cleanDirectory(output);
        } catch (Exception ignored) {
            //ignored
        }
        this.spoonModel = instrument(program.getAbsoluteSourceCodeDir(), program.getAbsoluteTestSourceCodeDir(), dependencies);
        SpoonModelBuilder modelBuilder = spoonModel.getModelBuilder();
        modelBuilder.setBinaryOutputDirectory(output);
        boolean status = modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
        DSpotUtils.copyResources(configuration);
        if (!status) {
            throw new RuntimeException("Error during compilation");
        }
    }

    private void instrumentAndCompileTest(CtMethod<?> testMethod){
        new TestInstrumentation(spoonModel.getFactory()).process(testMethod);
        spoonModel.getFactory().Class().get(testMethod.getParent(CtClass.class).getQualifiedName()).addMethod(testMethod);
        SpoonModelBuilder modelBuilder = spoonModel.getModelBuilder();
        File output = new File(pathToOutput);
        try {
            FileUtils.cleanDirectory(output);
        } catch (Exception ignored) {
            //ignored
        }
        modelBuilder.setBinaryOutputDirectory(output);
        boolean status = modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
    }

    private Launcher instrument(String pathToSources, String pathToTestSources, String dependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = (pathToSources +
                AmplificationHelper.PATH_SEPARATOR + pathToTestSources +
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
        return launcher;
    }
}
