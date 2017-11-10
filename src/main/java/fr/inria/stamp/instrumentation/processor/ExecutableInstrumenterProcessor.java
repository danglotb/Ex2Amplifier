package fr.inria.stamp.instrumentation.processor;

import fr.inria.stamp.instrumentation.util.InstrumenterHelper;
import fr.inria.stamp.instrumentation.util.TypeUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;



/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/11/17
 */
public class ExecutableInstrumenterProcessor extends InstrumenterProcessor<CtExecutable<?>> {

    @Override
    public void process(CtExecutable<?> executable) {
        if (!executable.getParameters().isEmpty() &&
                executable.getParameters().stream()
                        .map(CtParameter::getType)
                        .map(CtTypeReference::getTypeDeclaration)
                        .allMatch(TypeUtils::isSupported)) {
            final Factory factory = executable.getFactory();
            final CtClass<?> modelBuilderClass = factory
                    .Class().get("fr.inria.stamp.alloy.builder.ModelBuilder");

            final CtExecutableReference<?> depopParameters = modelBuilderClass
                    .getMethodsByName("depopParameters").get(0)
                    .getReference();

            final CtInvocation<?> invocation = factory.createInvocation(
                    factory.createTypeAccess(modelBuilderClass.getReference()),
                    depopParameters
            );

            executable.getParameters().stream()
                    .filter(parameter -> TypeUtils.isSupported(parameter.getType().getTypeDeclaration()))
                    .map(parameter -> InstrumenterHelper.convertParameterToConstructorCall(parameter.getSimpleName(), parameter.getType().getTypeDeclaration()))
                    .forEach(invocation::addArgument);

            executable.getBody().insertBegin(invocation);
        }
    }

}
