package fr.inria.stamp.catg;

import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import fr.inria.stamp.Utils;
import fr.inria.stamp.ex2amplifier.Ex2Amplifier;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
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

    public static CtMethod<?> generateMainMethodFromTestMethod(CtMethod<?> testMethod, CtType<?> testClass) {
        final Factory factory = testMethod.getFactory();
        final CtBlock<?> blockMain =
                new AssertionRemover().removeAssertion(testMethod).getBody().clone();
        final List<CtLiteral<?>> originalLiterals =
                blockMain.getElements(Ex2Amplifier.CT_LITERAL_TYPE_FILTER);
        final int[] count = new int[]{1};
        final List<CtLocalVariable<?>> localVariables =
                originalLiterals.stream()
                        .map(literal ->
                                factory.createLocalVariable(
                                        Utils.getRealTypeOfLiteral(literal),
                                        "lit" + count[0]++,
                                        factory.createCodeSnippetExpression(createMakeRead(factory, literal))
                                )
                        ).collect(Collectors.toList());
        final CtMethod<?> mainMethod = initMainMethod(factory);

        Collections.reverse(localVariables);
        localVariables.forEach(blockMain::insertBegin);
        Collections.reverse(localVariables);

        final Iterator<CtLocalVariable<?>> iterator = localVariables.iterator();
        blockMain.getElements(Ex2Amplifier.CT_LITERAL_TYPE_FILTER)
                .forEach(literal ->
                        literal.replace(factory.createVariableRead(iterator.next().getReference(), false))
                );
        if (!testMethod.getThrownTypes().isEmpty()) {
            final CtTry largeTryCatchBlock = createLargeTryCatchBlock(testMethod.getThrownTypes(), factory);
            largeTryCatchBlock.setBody(blockMain);
            mainMethod.setBody(largeTryCatchBlock);
        } else {
            mainMethod.setBody(blockMain);
        }
        removeNonStaticElement(mainMethod, testClass);
        return mainMethod;
    }

    private static CtTry createLargeTryCatchBlock(Set<CtTypeReference<? extends Throwable>> thrownTypes, Factory factory) {
        final CtTry aTry = factory.createTry();
        thrownTypes.stream()
                .forEach(ctTypeReference ->
                        addCatchGivenExceptionToTry(ctTypeReference, factory, aTry, ctTypeReference.getSimpleName())
                );
        return aTry;
    }

    private static void removeNonStaticElement(final CtMethod<?> mainMethod, final CtType testClass) {
        final CtBlock<?> body = mainMethod.getBody();
        final Factory factory = mainMethod.getFactory();

        // 1 create a local variable of the test class
        final CtLocalVariable localVariableOfTestClass = factory.createLocalVariable(
                testClass.getReference(),
                Character.toLowerCase(testClass.getSimpleName().charAt(0)) + testClass.getSimpleName().substring(1),
                factory.createConstructorCall(testClass.getReference())
        );
        // 2 invoke setUp(@Before) at the begin if present
        final CtTry wrappedBefore = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, localVariableOfTestClass, "org.junit.Before");
        if (wrappedBefore != null) {
            body.insertBegin(wrappedBefore);
        }
        // 3 invoke tearDown(@After) at the end of the block
        final CtTry wrappedAfter = wrapInTryCatchMethodWithSpecificAnnotation(testClass, factory, localVariableOfTestClass, "org.junit.After");
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
    }

    private static CtTry wrapInTryCatchMethodWithSpecificAnnotation(CtType testClass, Factory factory, CtLocalVariable localVariableOfTestClass, String fullQualifiedNameOfAnnoation) {
        final Optional<CtMethod<?>> setUpMethod = ((Set<CtMethod<?>>) testClass
                .getMethods())
                .stream().filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        fullQualifiedNameOfAnnoation.equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst();
        if (setUpMethod.isPresent()) {
            return wrapInTryCatch(
                    factory.createInvocation(
                            factory.createVariableRead(localVariableOfTestClass.getReference(), false),
                            setUpMethod.get().getReference()
                    ), factory.Type().createReference("java.lang.Exception")
            );
        } else {
            return null;
        }
    }

    private static CtTry wrapInTryCatch(CtStatement statementToBeWrapped, CtTypeReference exceptionType) {
        final Factory factory = statementToBeWrapped.getFactory();
        final CtTry aTry = factory.createTry();
        aTry.setBody(statementToBeWrapped);
        addCatchGivenExceptionToTry(exceptionType, factory, aTry, "");
        return aTry;
    }

    private static void addCatchGivenExceptionToTry(CtTypeReference exceptionType, Factory factory, CtTry aTry, String suffixNameException) {
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

    private static String createMakeRead(Factory factory, CtLiteral<?> literal) {
        Object value = literal.getValue();
        if (value instanceof String) {
            value = "\"" + ((String) value).replace("\"", "\\\"")
                    .replace("\n", "\" + System.getProperty(\"line.separator\") + \"")
                    + "\"";
        } else if (value instanceof Character) {
            value = "'" + value.toString().replace("\'", "\\\'") + "'";
        }
        final String type = Utils.getRealTypeOfLiteral(literal).getSimpleName();
        return "catg.CATG.read" +
                ("boolean".equals(literal.getType().getSimpleName()) ?
                        "Bool" : toU1.apply(type))
                + "((" + type + ")" + value + ")";
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
