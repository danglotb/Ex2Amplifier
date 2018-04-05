package eu.stamp.project.ex2amplifier.amplifier;

import eu.stamp.project.ex2amplifier.catg.CATGExecutor;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/01/18
 */
public abstract class Ex2Amplifier implements Amplifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ex2Amplifier.class);

    public enum Ex2Amplifier_Mode {
        CATG,
        JBSE
    }

    private String currentIntermediateOutputDirectoryPath;

    protected InputConfiguration configuration;

    protected CtType<?> currentTestClassToBeAmplified;

    protected Map<CtLiteral<?>, List<CtLiteral<?>>> intermediateAmplification;

    public static final TypeFilter<CtLiteral<?>> CT_LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<?>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<?> element) {
            return super.matches(element) && !"<nulltype>".equals(element.getType().getSimpleName());
        }
    };

    public void init(InputConfiguration configuration) {
        this.configuration = configuration;
        CATGExecutor.java_home = this.configuration.getProperty("java_home", "");
    }

    @Override
    public List<CtMethod> apply(CtMethod ctMethod) {
        this.intermediateAmplification = new LinkedHashMap<>();
        if (ctMethod.getElements(CT_LITERAL_TYPE_FILTER).isEmpty()) {
            return Collections.emptyList();
        }
        final List<CtMethod> amplifiedTests = this.internalApply(ctMethod)
                .stream()
                .filter(amplifiedTest -> !ctMethod.equals(amplifiedTest))
                .collect(Collectors.toList());
        if (!amplifiedTests.isEmpty()) {
            this.printIntermediateAmplifiedTests(ctMethod, amplifiedTests);
        }
        if (!this.intermediateAmplification.isEmpty()) {
            this.printIntermediateAmplification(ctMethod);
        }
        return amplifiedTests;
    }

    protected abstract List<CtMethod> internalApply(CtMethod ctMethod);

    // this output is meant to be used in the manual analysis of the result
    // in order to understand how it succeed or why the Ex2Amplifier is failing to catch the change behavior
    private void printIntermediateAmplifiedTests(CtMethod<?> originalTestCase, List<CtMethod> amplifiedTests) {
        try (FileWriter writer =
                     new FileWriter(this.currentIntermediateOutputDirectoryPath + "/" +
                             originalTestCase.getSimpleName() + ".txt", false)) {
            writer.write(amplifiedTests.stream()
                    .map(CtMethod::toString)
                    .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // this output is meant to be used in the manual analysis of the result
    // it will track replacement
    private void printIntermediateAmplification(CtMethod<?> originalTestCase) {
        try (FileWriter writer =
                     new FileWriter(this.currentIntermediateOutputDirectoryPath + "/" +
                              originalTestCase.getSimpleName() + "_values.csv", false)) {
            writer.write(getIntermediate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getIntermediate() {
        return this.intermediateAmplification.keySet()
                .stream()
                .reduce("", (acc, currentKey) ->
                                acc + currentKey.toString() + "," +
                                        this.intermediateAmplification.get(currentKey)
                                                .stream()
                                                .map(CtLiteral::toString)
                                                .collect(Collectors.joining(",")) +
                                        AmplificationHelper.LINE_SEPARATOR,
                        String::concat
                );
    }

    @Override
    public void reset(CtType ctType) {
        this.currentTestClassToBeAmplified = ctType;
        this.prepareIntermediateOutputDirectory(ctType.getQualifiedName(), this.configuration.getOutputDirectory());
    }

    private void prepareIntermediateOutputDirectory(String qualifiedName, String outputDirectory) {
        try {
            final File outputDir = new File(outputDirectory + "/" + qualifiedName);
            if (!outputDir.exists()) {
                FileUtils.forceMkdir(outputDir);
            }
            this.currentIntermediateOutputDirectoryPath = outputDirectory + "/" + qualifiedName;
        } catch (Exception e) {
            LOGGER.error("Error when trying to create directory for intermediate output {}", qualifiedName);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    protected String printAndCompile(CtType<?> clone) {
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.pathToTmpTestSources));
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(this.configuration.getInputProgram().getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/"
                + this.configuration.getInputProgram().getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/"
                + this.configuration.getInputProgram().getTestClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + this.additionalClasspathElement();
        final String pathToBinTests = this.configuration.getInputProgram().getProgramDir() + "/" +
                this.configuration.getInputProgram().getTestClassesDir();
        try {
            FileUtils.forceDelete(new File(pathToBinTests + "/" +
                    clone.getQualifiedName().replace(".", "/") + ".class"));
        } catch (IOException ignored) {
            //ignored
        }
        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath, new File(pathToBinTests));
        return classpath;
    }

    protected String additionalClasspathElement() {
        return "";
    }

    public static Ex2Amplifier getEx2Amplifier(Ex2Amplifier_Mode mode) {
        if (Ex2Amplifier_Mode.CATG == mode) {
            return new CATGAmplifier();
        } else {
            return new JBSEAmplifier();
        }
    }
}
