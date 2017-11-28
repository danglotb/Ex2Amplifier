package fr.inria.stamp.instrumentation;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.stamp.alloy.model.Variable;
import fr.inria.stamp.instrumentation.processor.InvocationInstrumenterProcessor;
import fr.inria.stamp.instrumentation.util.InstrumenterHelper;
import fr.inria.stamp.instrumentation.util.TypeUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/11/17
 */
public class TestInstrumentation extends AbstractProcessor<CtMethod<?>> {

    private static final String PREFIX_NAME_INPUT_VECTOR = "InputVector.input_";

    private int[] index = new int[]{0};

    private final Factory factory;

    public TestInstrumentation(Factory factory) {
        this.factory = factory;
    }

    @Override
    public boolean isToBeProcessed(CtMethod<?> candidate) {
        return AmplificationChecker.isTest(candidate);
    }

    @Override
    public void process(CtMethod<?> testMethod) {//TODO redondancy of the code here with InvocationInstrumenterProcessor
        final CtInvocation<?> callToAddInputs = createCallToAddInputs(this.factory);
        testMethod.getElements(new TypeFilter<CtAbstractInvocation<?>>(CtAbstractInvocation.class) {
            @Override
            public boolean matches(CtAbstractInvocation<?> candidate) {
                boolean isAssert = false;
                if (candidate instanceof CtInvocation) {
                    isAssert = AmplificationChecker.isAssert((CtInvocation) candidate);
                }
                return !candidate.getParent(CtPackage.class).getQualifiedName().startsWith("fr.inria.stamp") &&
                        !candidate.getExecutable().getParameters().isEmpty() && !isAssert &&
                        candidate.getArguments().stream().map(CtExpression::getType)
                                .map(CtTypeReference::getTypeDeclaration)
                                .allMatch(TypeUtils::isSupported);
            }
        }).stream()
                .flatMap(ctAbstractInvocation ->
                        instrument(ctAbstractInvocation).stream()
                ).forEach(callToAddInputs::addArgument);
        testMethod.getBody().insertBegin(callToAddInputs);
    }

    private List<? extends CtConstructorCall<?>> instrument(CtAbstractInvocation<?> invocation) {
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

        final List<? extends CtConstructorCall<?>> variablesCreations = invocation.getExecutable().getParameters()
                .stream()
                .filter(reference -> TypeUtils.isSupported(reference.getTypeDeclaration()))
                .map(invocation.getExecutable().getParameters()::indexOf)
                .map(invocation.getArguments()::get)
                .map(expression -> new Variable(
                        PREFIX_NAME_INPUT_VECTOR + index[0]++,
                        TypeUtils.toAlloyType(expression.getType().getTypeDeclaration())))
                .map(variable ->
                        InstrumenterHelper.convertParameterToConstructorCall(variable.name, variable.type, factory))
                .collect(Collectors.toList());

        variablesCreations.forEach(ctInvocation::addArgument);

        if (invocation.getParent(CtBlock.class).getStatements().contains(invocation)) {
            ((CtStatement) invocation).insertBefore(ctInvocation);
        } else {
            invocation.getParent(CtStatement.class).insertBefore(ctInvocation);
        }

        return variablesCreations;
    }

    private CtInvocation<?> createCallToAddInputs(Factory factory) {
        final CtClass<?> modelBuilderClass = factory
                .Class().get("fr.inria.stamp.alloy.builder.ModelBuilder");
        final CtExecutableReference<?> addInputs = modelBuilderClass
                .getMethodsByName("addInputs").get(0)
                .getReference();
        return factory.createInvocation(
                factory.createTypeAccess(modelBuilderClass.getReference()),
                addInputs
        );
    }


}
