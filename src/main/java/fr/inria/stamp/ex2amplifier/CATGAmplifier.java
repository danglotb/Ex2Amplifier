package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.catg.CATGExecutor;
import fr.inria.stamp.catg.CATGUtils;
import fr.inria.stamp.catg.MainGenerator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/02/18
 */
public class CATGAmplifier extends Ex2Amplifier {

    @Override
    public List<CtMethod> internalApply(CtMethod ctMethod) {
        final CtType<?> testClass = this.currentTestClassToBeAmplified.clone();
        this.currentTestClassToBeAmplified.getPackage().addType(testClass);
        final CtMethod<?> mainMethodFromTestMethod =
                MainGenerator.generateMainMethodFromTestMethod(ctMethod, testClass);
        testClass.addMethod(mainMethodFromTestMethod);
        final String classpath = printAndCompile(testClass);
        final List<List<String>> execute = CATGExecutor.execute(classpath, testClass.getQualifiedName());
        CATGUtils.eraseOldFiles();
        testClass.removeMethod(mainMethodFromTestMethod);
        return execute.stream()
                .map(values -> buildMethodFromValues(values, ctMethod))
                .collect(Collectors.toList());
    }

    private CtMethod<?> buildMethodFromValues(List<String> values, CtMethod originalTestMethod) {
        final Iterator<String> iteratorOnNewValues = values.iterator();
        final CtMethod<?> clone = new AssertionRemover().removeAssertion(
                AmplificationHelper.cloneMethodTest(originalTestMethod, "_Ex2_CATG")
        );
        final List<CtLiteral<?>> originalLiterals = clone.getBody().getElements(Ex2Amplifier.CT_LITERAL_TYPE_FILTER);
        originalLiterals.forEach(ctLiteral ->
                ctLiteral.replace(buildNewLiteralFromString(iteratorOnNewValues.next(), ctLiteral))
        );
        return clone;
    }

    private CtLiteral<?> buildNewLiteralFromString(String value, CtLiteral<?> literalToBeReplaced) {
        final Object originalLiteralValue = literalToBeReplaced.getValue();
        final Factory factory = literalToBeReplaced.getFactory();
        final CtLiteral<?> newLiteral;
        if (originalLiteralValue instanceof String) {
            newLiteral = factory.createLiteral(value);
        } else if (originalLiteralValue instanceof Integer) {
            newLiteral = factory.createLiteral(Integer.parseInt(value));
        } else if (originalLiteralValue instanceof Boolean) {
            newLiteral = factory.createLiteral(value.equals("true"));
        } else if (originalLiteralValue instanceof Character) {
            newLiteral = factory.createLiteral(value.charAt(0));
        } else {
            throw new UnsupportedOperationException(originalLiteralValue.getClass() + " is not supported");
        }
        if (!this.intermediateAmplification.containsKey(literalToBeReplaced)) {
            this.intermediateAmplification.put(literalToBeReplaced, new ArrayList<>());
        }
        this.intermediateAmplification.get(literalToBeReplaced).add(newLiteral);
        return newLiteral;
    }

    @Override
    protected String additionalClasspathElement() {
        return "lib/catg-dev.jar";
    }
}
