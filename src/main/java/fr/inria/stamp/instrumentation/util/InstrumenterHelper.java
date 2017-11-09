package fr.inria.stamp.instrumentation.util;

import fr.inria.stamp.alloy.model.Variable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/11/17
 */
public class InstrumenterHelper {

    public static CtConstructorCall<?> convertParameterToConstructorCall(String variable, CtType<?> type) {
        final Factory factory = type.getFactory();
        final CtClass<Variable> parameter = factory.Class().get("fr.inria.stamp.alloy.model.Variable");
        final CtConstructorCall<Variable> constructorCall = factory.createConstructorCall();
        constructorCall.setType(parameter.getReference());
        constructorCall.addArgument(factory.createLiteral(variable));
        constructorCall.addArgument(factory.createLiteral(TypeUtils.toAlloyType(type)));
        return constructorCall;
    }

    public static CtConstructorCall<?> convertParameterToConstructorCall(String variable, String type, Factory factory) {
        final CtClass<Variable> parameter = factory.Class().get("fr.inria.stamp.alloy.model.Variable");
        final CtConstructorCall<Variable> constructorCall = factory.createConstructorCall();
        constructorCall.setType(parameter.getReference());
        constructorCall.addArgument(factory.createLiteral(variable));
        constructorCall.addArgument(factory.createLiteral(type));
        return constructorCall;
    }

}
