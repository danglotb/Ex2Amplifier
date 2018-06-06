package eu.stamp_project.ex2amplifier.amplifier;

import eu.stamp_project.ex2amplifier.smt.SMTSolver;
import eu.stamp_project.ex2amplifier.jbse.ArgumentsExtractor;
import eu.stamp_project.ex2amplifier.jbse.JBSERunner;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/02/18
 */
public class JBSEAmplifier extends Ex2Amplifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(JBSEAmplifier.class);

    @Override
    public List<CtMethod> internalApply(CtMethod ctMethod) {
        final CtType<?> clone = this.currentTestClassToBeAmplified.clone();
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(ctMethod, clone);
        final List<CtParameter<?>> parameters = extractedMethod.getParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        this.currentTestClassToBeAmplified.getPackage().addType(clone);
        clone.removeMethod(ctMethod);
        clone.addMethod(extractedMethod);
        final String classpath = printAndCompile(clone);
        final List<Map<String, List<String>>> conditionForEachParameterForEachState =
                JBSERunner.runJBSE(classpath, extractedMethod)
                .stream()
                .filter(stringListMap ->
                        stringListMap.values()
                                .stream()
                                .noneMatch(strings ->
                                        strings.stream()
                                                .anyMatch(str -> str.contains("\\."))
                                )
                ).collect(Collectors.toList());
        return conditionForEachParameterForEachState.stream()
                .filter(condition -> ! condition.isEmpty())
                .map(conditions ->
                        this.generateNewTestMethod(ctMethod, conditions, parameters)
                ).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // the parameters list will be given to initialize variables in SMT solver
    private CtMethod<?> generateNewTestMethod(CtMethod<?> testMethod,
                                              Map<String, List<String>> conditionForParameter,
                                              List<CtParameter<?>> parameters) {
        final CtMethod clone = AmplificationHelper.cloneTestMethodForAmp(testMethod, "_Ex2_JBSE");
        final List<?> solutions = SMTSolver.solve(conditionForParameter, parameters.size());
        final Iterator<?> iterator = solutions.iterator();
        final List<CtLiteral> originalLiterals =
                clone.getElements(literal -> !(literal.getValue() instanceof String));
        conditionForParameter.keySet()
                .forEach(s -> {
                    try {
                        final int indexOfLit = Integer.parseInt(s.substring("param".length()));
                        final CtLiteral literalToBeReplaced = originalLiterals.get(indexOfLit);
                        final CtLiteral<?> newLiteral = testMethod.getFactory().createLiteral(iterator.next());
                        if (!this.intermediateAmplification.containsKey(literalToBeReplaced)) {
                            this.intermediateAmplification.put(literalToBeReplaced, new ArrayList<>());
                        }
                        this.intermediateAmplification.get(literalToBeReplaced).add(newLiteral);
                        if (literalToBeReplaced.getParent() instanceof CtUnaryOperator) {
                            literalToBeReplaced.getParent().replace(newLiteral);
                        } else {
                            literalToBeReplaced.replace(newLiteral);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Error when trying to generate a value for {}", s);
                    }
                });
        return clone;
    }
}
