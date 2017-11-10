package fr.inria.stamp.instrumentation.processor;

import fr.inria.stamp.instrumentation.util.Translator;
import fr.inria.stamp.instrumentation.util.TypeUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/11/17
 */
public class ModificationInstrumenterProcessor extends InstrumenterProcessor<CtAssignment> {

    @Override
    public void process(CtAssignment assignment) {
        final CtClass<?> modelBuilderClass = assignment.getFactory()
                .Class().get("fr.inria.stamp.alloy.builder.ModelBuilder");
        final CtExecutableReference<?> addModification = modelBuilderClass
                .getMethodsByName("addModification").get(0)
                .getReference();
        final CtThisAccess<?> thisAccess = assignment.getFactory().createThisAccess(modelBuilderClass.getReference());
        final CtLiteral<String> assigned = assignment.getFactory().createLiteral(Translator.toAlloy(assignment.getAssigned()));
        final CtLiteral<String> alloyModification = assignment.getFactory().createLiteral(Translator.toAlloy(assignment));

        final List<CtExpression<?>> parameters = new ArrayList<>();
        parameters.add(thisAccess);
        parameters.add(assigned);
        parameters.add(alloyModification);
        parameters.addAll(creationOfAttributes(assignment.getParent(CtType.class))); // TODO checks this for internal class
        assignment.insertAfter(
                assignment.getFactory().createInvocation(assignment
                                .getFactory()
                                .createTypeAccess(modelBuilderClass.getReference()),
                        addModification,
                        parameters
                )
        );
    }

    private List<CtExpression<?>> creationOfAttributes(CtType<?> ctType) {
        final Factory factory = ctType.getFactory();
        final CtTypeReference<?> attributeReference =
                factory.Type().get("fr.inria.stamp.alloy.model.Variable").getReference();
        return ctType.getFields().stream()
                .filter(ctField -> TypeUtils.isSupported(ctField.getType().getTypeDeclaration()))
                .map(ctField ->
                        factory.createConstructorCall(attributeReference,
                                factory.createLiteral(ctField.getSimpleName()),
                                factory.createLiteral(TypeUtils.toAlloyType(ctField.getType().getTypeDeclaration()))
                        )
                ).collect(Collectors.toList());
    }

}
