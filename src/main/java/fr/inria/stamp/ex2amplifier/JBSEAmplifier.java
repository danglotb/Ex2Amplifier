package fr.inria.stamp.ex2amplifier;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.jbse.ArgumentsExtractor;
import fr.inria.stamp.jbse.JBSERunner;
import fr.inria.stamp.smt.SMTSolver;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/02/18
 */
class JBSEAmplifier implements Amplifier {

    private InputConfiguration configuration;

    private CtType<?> currentTestClassToBeAmplified;

    JBSEAmplifier(InputConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CtMethod> apply(CtMethod ctMethod) {
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(ctMethod);
        if (extractedMethod.getParameters().isEmpty()) {
            return Collections.emptyList();
        }
        final CtType<?> clone = this.currentTestClassToBeAmplified.clone();
        clone.setParent(this.currentTestClassToBeAmplified.getParent());
        clone.removeMethod(ctMethod);
        clone.addMethod(extractedMethod);
        DSpotUtils.printJavaFileWithComment(clone, new File(DSpotCompiler.pathToTmpTestSources));
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(this.configuration.getInputProgram().getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/" + this.configuration.getInputProgram().getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/" + this.configuration.getInputProgram().getTestClassesDir();
        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(this.configuration.getInputProgram().getProgramDir() + "/" + this.configuration.getInputProgram().getTestClassesDir()));
        final List<Map<String, List<String>>> conditionForEachParameterForEachState = JBSERunner.runJBSE(classpath, extractedMethod);
        return conditionForEachParameterForEachState.stream()
                .map(conditions ->
                        this.generateNewTestMethod(ctMethod, conditions)
                )
                .collect(Collectors.toList());
    }

    private CtMethod<?> generateNewTestMethod(CtMethod<?> testMethod,
                                              Map<String, List<String>> conditionForParameter) {
        final CtMethod clone = AmplificationHelper.cloneMethodTest(testMethod, "_examplifier");
        final List<?> solutions = SMTSolver.solve(conditionForParameter);
        final Iterator<?> iterator = solutions.iterator();
        final List<CtLiteral> originalLiterals =
                clone.getElements(new TypeFilter<>(CtLiteral.class));
        conditionForParameter.keySet()
                .forEach(s -> {
                    final int indexOfLit = Integer.parseInt(s.substring("param".length()));
                    final CtLiteral literalToBeReplaced = originalLiterals.get(indexOfLit);
                    final CtLiteral<?> newLiteral = testMethod.getFactory().createLiteral(iterator.next());
                    if (literalToBeReplaced.getParent() instanceof CtUnaryOperator) {
                        literalToBeReplaced.getParent().replace(newLiteral);
                    } else {
                        literalToBeReplaced.replace(newLiteral);
                    }
                });
        return clone;
    }

    private final Predicate<CtMethod<?>> canBeRun = ctMethod ->
            ctMethod.getParameters()
                    .stream()
                    .map(CtParameter::getType)
                    .map(CtTypeReference::getSimpleName)
                    .allMatch(JBSERunner.typeToDescriptor::containsKey
                            // || "String".equals(typeStr)  TODO for now, we do not support String, because JBSE might take long time
                    );

    @Override
    public void reset(CtType ctType) {
        this.currentTestClassToBeAmplified = ctType;
    }
}
