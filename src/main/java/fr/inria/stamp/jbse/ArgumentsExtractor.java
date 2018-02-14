package fr.inria.stamp.jbse;

import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

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


    public static CtMethod<?> performExtraction(CtMethod<?> ctMethod) {

        // remove assertion

        final CtMethod<?> ctMethodWithoutAssertion =
                new AssertionRemover().removeAssertion(ctMethod);

        final Factory factory = ctMethodWithoutAssertion.getFactory();
        final CtMethod<?> extractedMethod = ctMethodWithoutAssertion.clone();

        extractedMethod.setSimpleName("extract_" + ctMethodWithoutAssertion.getSimpleName());
        new ArrayList<>(extractedMethod.getThrownTypes()).forEach(extractedMethod::removeThrownType);
        extractedMethod.setParent(ctMethod.getParent());
        ctMethodWithoutAssertion.getAnnotations().forEach(extractedMethod::removeAnnotation);
        final int[] count = new int[1];
        final Map<CtAbstractInvocation<?>, List<CtVariableAccess>> parametersPerInvocation =
                new HashMap<>();
        extractedMethod.getElements(new TypeFilter<>(CtLiteral.class))
                .stream()
                .filter(ctLiteral -> !(ctLiteral.getValue() instanceof String)) // TODO
                .forEach(ctLiteral -> {
                    final CtParameter<?> parameter = factory.createParameter();
                    parameter.setType(ctLiteral.getType());
                    parameter.setSimpleName("lit" + count[0]++);
                    extractedMethod.addParameter(parameter);
                    final CtVariableAccess<?> variableRead = factory.createVariableRead(factory.createParameterReference(parameter), false);
                    final CtAbstractInvocation invocation = ctLiteral.getParent(CtAbstractInvocation.class);
                    if (invocation != null) {
                        if (!parametersPerInvocation.containsKey(invocation)) {
                            parametersPerInvocation.put(invocation, new ArrayList<>(invocation.getArguments()));
                        }
                        if (ctLiteral.getParent() instanceof CtUnaryOperator) {
                            final int index = invocation.getArguments().indexOf(ctLiteral.getParent());
                            parametersPerInvocation.get(invocation).remove(index);
                            parametersPerInvocation.get(invocation).add(index, variableRead);
                        } else {
                            final int index = invocation.getArguments().indexOf(ctLiteral);
                            parametersPerInvocation.get(invocation).remove(index);
                            parametersPerInvocation.get(invocation).add(index, variableRead);
                        }
                    } else {
                        ctLiteral.replace(variableRead);
                    }
                });

        extractedMethod.getElements(new TypeFilter<>(CtAbstractInvocation.class))
                .stream()
                .filter(parametersPerInvocation::containsKey)
                .forEach(ctAbstractInvocation ->
                        ctAbstractInvocation.setArguments(parametersPerInvocation.get(ctAbstractInvocation))
                );
        extractedMethod.setThrownTypes(ctMethod.getThrownTypes());
        return extractedMethod;
    }
}
