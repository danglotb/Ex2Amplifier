package eu.stamp_project.ex2amplifier;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/02/18
 */
public class Utils {

    public static CtTypeReference<?> getRealTypeOfLiteral(CtLiteral<?> literal) {
        if (literal.getValue() instanceof Number) {
            final CtTypedElement typedParent = literal.getParent(CtTypedElement.class);
            if (typedParent != null) {// special treatment for int literal
                if (typedParent instanceof CtAbstractInvocation) {
                    final CtExecutableReference<?> executable = ((CtAbstractInvocation) typedParent).getExecutable();
                    int indexOf = ((CtAbstractInvocation) typedParent).getArguments().indexOf(literal);
                    if (indexOf >= executable.getParameters().size()) {
                        indexOf--; // TODO checks if this is correct in case of varargs
                    }
                    final CtTypeReference<?> ctTypeReference = executable.getParameters().get(indexOf);
                    if (Number.class.isAssignableFrom(ctTypeReference.getActualClass())) {
                        return ctTypeReference;
                    } else {
                        return literal.getType().unbox();
                    }
                } else if (typedParent.getType() instanceof CtArrayTypeReference) {
                    return ((CtArrayTypeReference) typedParent.getType()).getComponentType().unbox();
                } else if (typedParent instanceof CtBinaryOperator) {
                    return literal.getType().unbox();
                } else {
                    return typedParent.getType().unbox();
                }
            } else {
                throw new IllegalArgumentException(literal.toString());
            }
        } else {
            return literal.getType().unbox();
        }
    }

    public static void removeNonStaticElement(final CtMethod<?> mainMethod, final CtType testClass) {
        final CtBlock<?> body = mainMethod.getBody();
        final Factory factory = mainMethod.getFactory();

        // 1 create a local variable of the test class
        final CtLocalVariable localVariableOfTestClass = factory.createLocalVariable(
                testClass.getReference(),
                Character.toLowerCase(testClass.getSimpleName().charAt(0)) + testClass.getSimpleName().substring(1),
                factory.createConstructorCall(testClass.getReference())
        );
        // 2 invoke setUp(@Before) at the begin if present
        final CtExpression targetForCalls = factory.createVariableRead(localVariableOfTestClass.getReference(), false);
        final CtTry wrappedBefore = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, targetForCalls, "org.junit.Before");
        if (wrappedBefore != null) {
            body.insertBegin(wrappedBefore);
        }
        // 3 invoke tearDown(@After) at the end of the block
        final CtTry wrappedAfter = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, targetForCalls, "org.junit.After");
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
                        target.replace(factory.createVariableRead(localVariableOfTestClass.getReference(), false))
                );

        body.insertBegin(localVariableOfTestClass);

        // 5 invoke setUpBeforeClass(@BeforeClass)
        final CtTry wrappedBeforeClass = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, factory.createTypeAccess(testClass.getReference()), "org.junit.BeforeClass");
        if (wrappedBeforeClass != null) {
            body.insertBegin(wrappedBeforeClass);
        }

        // 6 invoke tearDownAfterClass(@AfterClass)
        final CtTry wrappedAfterClass = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, factory.createTypeAccess(testClass.getReference()), "org.junit.AfterClass");
        if (wrappedAfterClass != null) {
            body.insertEnd(wrappedAfterClass);
        }
    }

    public static CtTry wrapInTryCatchMethodWithSpecificAnnotation(CtType testClass,
                                                                   Factory factory,
                                                                   CtExpression<?> target,
                                                                   String fullQualifiedNameOfAnnotation) {
        final Optional<CtMethod<?>> methodWithGivenAnnotation = ((Set<CtMethod<?>>) testClass
                .getMethods())
                .stream().filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        fullQualifiedNameOfAnnotation.equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst();
        return methodWithGivenAnnotation.map(ctMethod -> wrapInTryCatch(
                factory.createInvocation(
                        target,
                        ctMethod.getReference()
                ), factory.Type().createReference("java.lang.Exception")
        )).orElse(null);
    }

    public static CtTry wrapInTryCatch(CtStatement statementToBeWrapped, CtTypeReference exceptionType) {
        final Factory factory = statementToBeWrapped.getFactory();
        final CtTry aTry = factory.createTry();
        aTry.setBody(statementToBeWrapped);
        addCatchGivenExceptionToTry(exceptionType, factory, aTry, "");
        return aTry;
    }

    public static void addCatchGivenExceptionToTry(CtTypeReference exceptionType, Factory factory, CtTry aTry, String suffixNameException) {
        final CtCatch aCatch = factory.createCatch();
        final CtCatchVariable catchVariable = factory.createCatchVariable();
        catchVariable.setSimpleName("__exceptionEx2Amplifier" + suffixNameException);
        catchVariable.setType(exceptionType);
        aCatch.setParameter(catchVariable);
        final CtThrow aThrow = factory.createThrow();
        aThrow.setThrownExpression(
                factory.createConstructorCall(factory.Type().createReference("java.lang.RuntimeException"),
                        factory.createVariableRead(factory.createCatchVariableReference(catchVariable), false)
                )
        );
        aCatch.setBody(aThrow);
        aTry.addCatcher(aCatch);
    }
}
