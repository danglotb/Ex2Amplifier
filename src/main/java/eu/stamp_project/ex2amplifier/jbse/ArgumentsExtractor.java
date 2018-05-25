package eu.stamp_project.ex2amplifier.jbse;

import eu.stamp_project.ex2amplifier.Utils;
import eu.stamp_project.dspot.assertGenerator.AssertionRemover;
import eu.stamp_project.ex2amplifier.amplifier.Ex2Amplifier;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtParameterReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class ArgumentsExtractor {


    public static CtMethod<?> performExtraction(CtMethod<?> ctMethod, CtType<?> testClass) {
        final CtMethod<?> ctMethodWithoutAssertion =
                new AssertionRemover().removeAssertion(ctMethod);

        final Factory factory = ctMethodWithoutAssertion.getFactory();
        final CtMethod<?> extractedMethod = ctMethodWithoutAssertion.clone();

        extractedMethod.setSimpleName("extract_" + ctMethodWithoutAssertion.getSimpleName());
        new ArrayList<>(extractedMethod.getThrownTypes()).forEach(extractedMethod::removeThrownType);
        ctMethodWithoutAssertion.getAnnotations().forEach(extractedMethod::removeAnnotation);
        final int[] count = new int[1];
        final Map<CtAbstractInvocation<?>, List<CtVariableAccess>> parametersPerInvocation =
                new HashMap<>();
        extractedMethod.getElements(Ex2Amplifier.CT_LITERAL_TYPE_FILTER).stream()
                .filter(literal ->! (literal.getValue() instanceof String))
                .forEach(ctLiteral -> {
                    final CtParameter parameter = factory.createParameter(extractedMethod, Utils.getRealTypeOfLiteral(ctLiteral), "lit" + count[0]++);
                    final CtParameterReference<?> parameterReference = factory.createParameterReference();
                    parameterReference.setSimpleName(parameter.getSimpleName());
                    parameterReference.setType(parameter.getType());
                    final CtVariableAccess<?> variableRead = factory.createVariableRead(parameterReference, false);
                    final CtAbstractInvocation invocation = ctLiteral.getParent(CtAbstractInvocation.class);
                    if (invocation != null) {
                        if (!parametersPerInvocation.containsKey(invocation)) {
                            parametersPerInvocation.put(invocation, new ArrayList<>(invocation.getArguments()));
                        }
                        if (ctLiteral.getParent() instanceof CtUnaryOperator) {
                            ctLiteral.getParent().replace(variableRead);
                        } else {
                            ctLiteral.replace(variableRead);
                        }
                    } else {
                        ctLiteral.replace(variableRead);
                    }
                });
        extractedMethod.setThrownTypes(ctMethod.getThrownTypes());
        Utils.removeNonStaticElement(extractedMethod, testClass);
        return extractedMethod;
    }
}
