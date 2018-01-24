package fr.inria.stamp;

import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
        return mainMethod;
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
