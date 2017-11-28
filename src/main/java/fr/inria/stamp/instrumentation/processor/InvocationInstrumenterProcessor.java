package fr.inria.stamp.instrumentation.processor;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.stamp.instrumentation.util.InstrumenterHelper;
import fr.inria.stamp.instrumentation.util.TypeUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/11/17
 */
public class InvocationInstrumenterProcessor extends InstrumenterProcessor<CtAbstractInvocation<?>> {

    @Override
    public boolean isToBeProcessed(CtAbstractInvocation<?> candidate) {
        boolean isAssert = false;
        if (candidate instanceof CtInvocation) {
            isAssert = AmplificationChecker.isAssert((CtInvocation) candidate);
        }
        return super.isToBeProcessed(candidate) &&
                !candidate.getExecutable().getParameters().isEmpty() && !isAssert
                && candidate.getArguments().stream().map(CtExpression::getType)
                .map(CtTypeReference::getTypeDeclaration)
                .allMatch(TypeUtils::isSupported);
    }

    @Override
    public void process(CtAbstractInvocation<?> invocation) {

        final Factory factory = invocation.getFactory();
        final CtClass<?> modelBuilderClass = factory
                .Class().get("fr.inria.stamp.alloy.builder.ModelBuilder");

        final CtExecutableReference<?> addParameters = modelBuilderClass
                .getMethodsByName("addParameters").get(0)
                .getReference();

        final CtInvocation<?> ctInvocation = factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                addParameters
        );

        ctInvocation.setType(factory.Type().createReference("fr.inria.stamp.alloy.model.Parameter"));

        invocation.getExecutable().getParameters()
                .stream()
                .filter(reference ->
                        TypeUtils.isSupported(reference.getTypeDeclaration())
                ).map(invocation.getExecutable().getParameters()::indexOf)
                .map(invocation.getArguments()::get)
                .map(expression -> InstrumenterHelper.convertParameterToConstructorCall(expression.toString(), expression.getType().getTypeDeclaration()))
                .forEach(ctInvocation::addArgument);
        if (invocation.getParent(CtBlock.class).getStatements().contains(invocation)) {
            ((CtStatement)invocation).insertBefore(ctInvocation);
        } else {
            invocation.getParent(CtStatement.class).insertBefore(ctInvocation);
        }
    }

}
