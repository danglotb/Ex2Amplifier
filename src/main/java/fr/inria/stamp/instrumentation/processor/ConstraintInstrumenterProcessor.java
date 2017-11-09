package fr.inria.stamp.instrumentation.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

import static fr.inria.stamp.instrumentation.util.Translator.toAlloy;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/11/17
 */
public class ConstraintInstrumenterProcessor extends AbstractProcessor<CtIf> {

    @Override
    public void process(CtIf ctIf) {
        final CtClass<?> modelBuilderClass = ctIf.getFactory()
                .Class().get("fr.inria.stamp.alloy.builder.ModelBuilder");
        final CtExecutableReference<?> addConstraintMethod = modelBuilderClass
                .getMethodsByName("addConstraint").get(0)
                .getReference();
        final CtExecutableReference<?> endConstraintMethod = modelBuilderClass
                .getMethodsByName("endConstraint").get(0)
                .getReference();
        instrumentIfBlock(ctIf, addConstraintMethod, modelBuilderClass, endConstraintMethod);
        instrumentElseBlock(ctIf, addConstraintMethod, modelBuilderClass, endConstraintMethod);
    }

    private void instrumentIfBlock(CtIf ctIf,
                                          CtExecutableReference<?> addConstraintMethod,
                                          CtClass<?> modelBuilderClass,
                                          CtExecutableReference<?> endConstraintMethod) {
        final Factory factory = ctIf.getFactory();
        if (!(ctIf.getThenStatement() instanceof CtBlock)) {
            ctIf.getThenStatement().replace(factory.createCtBlock(ctIf.getThenStatement().clone()));
        }
        final CtInvocation<?> invocationToAddConstraint = factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                addConstraintMethod
        );
        final CtThisAccess<?> thisAccess = factory.createThisAccess(modelBuilderClass.getReference());
        invocationToAddConstraint.addArgument(thisAccess);
        invocationToAddConstraint.addArgument(factory.createLiteral(toAlloy(ctIf.getCondition())));
        ((CtBlock<?>) ctIf.getThenStatement()).insertBegin(invocationToAddConstraint);

        final CtInvocation<?> invocationToEndConstraint = factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                endConstraintMethod
        );
        ((CtBlock<?>) ctIf.getThenStatement()).insertEnd(invocationToEndConstraint);
    }

    private void instrumentElseBlock(CtIf ctIf,
                                            CtExecutableReference<?> addConstraintMethod,
                                            CtClass<?> modelBuilderClass,
                                            CtExecutableReference<?> endConstraintMethod) {
        final Factory factory = ctIf.getFactory();
        final CtUnaryOperator<Boolean> negationCondition = factory.createUnaryOperator();
        negationCondition.setKind(UnaryOperatorKind.NOT);
        negationCondition.setOperand(ctIf.getCondition());
        final CtInvocation<?> invocationToAddConstraintInElse = factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                addConstraintMethod
        );
        final CtThisAccess<?> thisAccess = factory.createThisAccess(modelBuilderClass.getReference());
        invocationToAddConstraintInElse.addArgument(thisAccess);
        invocationToAddConstraintInElse.addArgument(factory.createLiteral(toAlloy(negationCondition)));
        if (ctIf.getElseStatement() == null) {
            ctIf.setElseStatement(factory.createCtBlock(invocationToAddConstraintInElse));
        } else {
            if (!(ctIf.getElseStatement() instanceof CtBlock)) {
                ctIf.getElseStatement().replace(factory.createCtBlock(ctIf.getElseStatement().clone()));
            }
            ((CtBlock<?>) ctIf.getElseStatement()).insertBegin(invocationToAddConstraintInElse);
        }
        final CtInvocation<?> invocationToEndConstraint = factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                endConstraintMethod
        );
        ((CtBlock<?>) ctIf.getElseStatement()).insertEnd(invocationToEndConstraint);
    }

}
