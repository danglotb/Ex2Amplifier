package eu.stamp_project.ex2amplifier.catg;

import eu.stamp_project.ex2amplifier.Utils;
import eu.stamp_project.ex2amplifier.amplifier.Ex2Amplifier;
import eu.stamp_project.dspot.assertGenerator.AssertionRemover;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
        Utils.removeNonStaticElement(mainMethod, testClass);
        return mainMethod;
    }

    private static CtTry createLargeTryCatchBlock(Set<CtTypeReference<? extends Throwable>> thrownTypes, Factory factory) {
        final CtTry aTry = factory.createTry();
        thrownTypes.stream()
                .forEach(ctTypeReference ->
                        Utils.addCatchGivenExceptionToTry(ctTypeReference, factory, aTry, ctTypeReference.getSimpleName())
                );
        return aTry;
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
        String readInvocation = "catg.CATG.read";
        if ("boolean".equals(type.toLowerCase())) {
            readInvocation += "Bool";
        } else if ("integer".equals(type.toLowerCase())) {
            readInvocation += "Int";
        } else {
            readInvocation += toU1.apply(type);
        }
        return readInvocation + "((" + type + ")" + value + ")";
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
