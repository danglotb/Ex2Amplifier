package eu.stamp_project.ex2amplifier.jbse;

import eu.stamp_project.ex2amplifier.Utils;
import eu.stamp_project.dspot.assertGenerator.AssertionRemover;
import eu.stamp_project.ex2amplifier.amplifier.Ex2Amplifier;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtParameterReference;
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
        removeNonStaticElement(extractedMethod, testClass);
        return extractedMethod;
    }


    private static void removeNonStaticElement(final CtMethod<?> mainMethod, final CtType<?> testClass) {
        final CtBlock<?> body = mainMethod.getBody();
        final Factory factory = mainMethod.getFactory();

        // create this access
        final CtThisAccess<?> thisAccess = factory.createThisAccess(testClass.getReference());
        // 2 invoke setUp(@Before) at the begin if present
        final CtTry wrappedBefore = Utils.wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, thisAccess, "org.junit.Before");
        if (wrappedBefore != null) {
            body.insertBegin(wrappedBefore);
        }
        // 3 invoke tearDown(@After) at the end of the block
        final CtTry wrappedAfter = Utils.wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, thisAccess, "org.junit.After");
        if (wrappedAfter != null) {
            body.insertEnd(wrappedAfter);
        }
        // 4 replaces all non-static accesses to accesses on the local variable created at the first step
        body.getElements(new TypeFilter<CtTargetedExpression>(CtTargetedExpression.class) {
            @Override
            public boolean matches(CtTargetedExpression element) {
                return element.getTarget() instanceof CtThisAccess;
            }
        }).stream()
                .map(CtTargetedExpression::getTarget)
                .forEach(target ->
                        target.replace(thisAccess.clone())
                );

        // 5 invoke setUpBeforeClass(@BeforeClass)
        final CtTry wrappedBeforeClass = Utils.wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, factory.createTypeAccess(testClass.getReference()), "org.junit.BeforeClass");
        if (wrappedBeforeClass != null) {
            body.insertBegin(wrappedBeforeClass);
        }

        // 6 invoke tearDownAfterClass(@AfterClass)
        final CtTry wrappedAfterClass = Utils.wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, factory.createTypeAccess(testClass.getReference()), "org.junit.AfterClass");
        if (wrappedAfterClass != null) {
            body.insertEnd(wrappedAfterClass);
        }
    }
}
