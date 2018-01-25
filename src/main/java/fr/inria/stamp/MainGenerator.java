package fr.inria.stamp;

import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class MainGenerator {

    public static CtMethod<?> generateMainMethodFromTestMethod(CtMethod<?> testMethod) {
        final Factory factory = testMethod.getFactory();
        final CtBlock<?> blockMain =
                new AssertionRemover().removeAssertion(testMethod).getBody().clone();
        final List<CtLiteral<?>> originalLiterals =
                blockMain.getElements(new TypeFilter<CtLiteral<?>>(CtLiteral.class));
        final int[] count = new int[]{1};
        final List<CtLocalVariable<?>> localVariables =
                originalLiterals.stream()
                        .map(literal ->
                                factory.createLocalVariable(
                                        literal.getType(),
                                        "lit" + count[0]++,
                                        factory.createCodeSnippetExpression(createMakeRead(factory, literal))
                                )
                        ).collect(Collectors.toList());
        final CtMethod<?> mainMethod = initMainMethod(factory);

        Collections.reverse(localVariables);
        localVariables.forEach(blockMain::insertBegin);
        Collections.reverse(localVariables);

        final Iterator<CtLocalVariable<?>> iterator = localVariables.iterator();
        blockMain.getElements(new TypeFilter<CtLiteral<?>>(CtLiteral.class))
                .forEach(literal ->
                        literal.replace(factory.createVariableRead(iterator.next().getReference(), false))
                );
        mainMethod.setBody(blockMain);
        removeNonStaticElement(mainMethod, testMethod.getParent(CtClass.class));
        return mainMethod;
    }

    private static void removeNonStaticElement(final CtMethod<?> mainMethod, final CtClass testClass) {
        final CtBlock<?> body = mainMethod.getBody();
        final Factory factory = mainMethod.getFactory();

        // 1 create a local variable of the test class
        final CtLocalVariable localVariableOfTestClass = factory.createLocalVariable(
                testClass.getReference(),
                Character.toLowerCase(testClass.getSimpleName().charAt(0)) + testClass.getSimpleName().substring(1),
                factory.createConstructorCall(testClass.getReference())
        );
        // 2 invoke setUp(@Before) at the begin if present
        insertSetUpAtBegin(testClass, body, factory, localVariableOfTestClass);
        // 3 invoke tearDown(@After) at the end of the block
        insertTearDownAtTheEnd(testClass, body, factory, localVariableOfTestClass);
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
    }

    private static void insertTearDownAtTheEnd(CtClass testClass, CtBlock<?> body, Factory factory, CtLocalVariable localVariableOfTestClass) {
        final Optional<CtMethod<?>> tearDownMethod = ((Set<CtMethod<?>>) testClass
                .getMethods())
                .stream().filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        "org.junit.After".equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst();
        if (tearDownMethod.isPresent()) {
            body.insertEnd(
                    wrappeInvocationInTryCatch(
                            factory.createInvocation(
                                    factory.createVariableRead(localVariableOfTestClass.getReference(), false),
                                    tearDownMethod.get().getReference()
                            )
                    ));
        }
    }

    private static void insertSetUpAtBegin(CtClass testClass, CtBlock<?> body, Factory factory, CtLocalVariable localVariableOfTestClass) {
        final Optional<CtMethod<?>> setUpMethod = ((Set<CtMethod<?>>) testClass
                .getMethods())
                .stream().filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        "org.junit.Before".equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst();
        if (setUpMethod.isPresent()) {
            body.insertBegin(
                    wrappeInvocationInTryCatch(
                            factory.createInvocation(
                                    factory.createVariableRead(localVariableOfTestClass.getReference(), false),
                                    setUpMethod.get().getReference()
                            )
                    ));
        }
    }

    private static CtTry wrappeInvocationInTryCatch(CtInvocation<?> invocationToBeWrapped) {
        final Factory factory = invocationToBeWrapped.getFactory();
        final CtTry aTry = factory.createTry();
        aTry.setBody(invocationToBeWrapped);
        final CtCatch aCatch = factory.createCatch();
        final CtCatchVariable catchVariable = factory.createCatchVariable();
        catchVariable.setSimpleName("__exceptionEx2Amplifier");
        catchVariable.setType(factory.Type().createReference("java.lang.Exception"));
        aCatch.setParameter(catchVariable);
        final CtThrow aThrow = factory.createThrow();
        aThrow.setThrownExpression(
                factory.createConstructorCall(factory.Type().createReference("java.lang.RuntimeException"),
                        factory.createVariableRead(factory.createCatchVariableReference(catchVariable), false)
                )
        );
        aCatch.setBody(aThrow);
        aTry.addCatcher(aCatch);
        return aTry;
    }

    private static String createMakeRead(Factory factory, CtLiteral<?> literal) {
        Object value = literal.getValue();
        if (value instanceof String) {
            value = "\"" + value + "\"";
        }
        return "catg.CATG.read" + toU1.apply(literal.getType().getSimpleName()) + "(" + value + ")";
    }

    private static final Function<String, String> toU1 = string ->
            Character.toUpperCase(string.charAt(0)) + string.substring(1);

    private static CtMethod<?> initMainMethod(Factory factory) {
        final CtMethod<Void> mainMethod = factory.createMethod();
        mainMethod.setSimpleName("main");
        mainMethod.addModifier(ModifierKind.PUBLIC);
        mainMethod.addModifier(ModifierKind.STATIC);
        mainMethod.setType(factory.Type().VOID_PRIMITIVE);
        final CtParameter parameter = factory.createParameter();
        parameter.setSimpleName("args");
        parameter.setType(factory.Type().get(String[].class).getReference());
        mainMethod.addParameter(parameter);
        return mainMethod;
    }

}
