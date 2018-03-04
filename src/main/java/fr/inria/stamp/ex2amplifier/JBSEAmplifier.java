package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.jbse.ArgumentsExtractor;
import fr.inria.stamp.jbse.JBSERunner;
import fr.inria.stamp.smt.SMTSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(ctMethod);
        if (extractedMethod.getParameters().isEmpty()) {
            return Collections.emptyList();
        }
        final CtType<?> clone = this.currentTestClassToBeAmplified.clone();
        clone.setParent(this.currentTestClassToBeAmplified.getParent());
        clone.removeMethod(ctMethod);
        clone.addMethod(extractedMethod);
        final String classpath = printAndCompile(clone);
        final List<Map<String, List<String>>> conditionForEachParameterForEachState = JBSERunner.runJBSE(classpath, extractedMethod);
        return conditionForEachParameterForEachState.stream()
                .filter(condition -> ! condition.isEmpty())
                .map(conditions ->
                        this.generateNewTestMethod(ctMethod, conditions)
                ).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CtMethod<?> generateNewTestMethod(CtMethod<?> testMethod,
                                              Map<String, List<String>> conditionForParameter) {
        final CtMethod clone = AmplificationHelper.cloneMethodTest(testMethod, "_Ex2_JBSE");
        final List<?> solutions = SMTSolver.solve(conditionForParameter);
        final Iterator<?> iterator = solutions.iterator();
        final List<CtLiteral> originalLiterals =
                clone.getElements(new TypeFilter<>(CtLiteral.class));
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
                    } catch (NoSuchElementException e) {
                        LOGGER.warn("No more element when trying to get literal for {}", s);
                    }
                });
        return clone;
    }
}
