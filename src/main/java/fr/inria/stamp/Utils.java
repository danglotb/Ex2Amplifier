package fr.inria.stamp;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/02/18
 */
public class Utils {

    public static CtTypeReference<?> getRealTypeOfLiteral(CtLiteral<?> literal) {
        if (literal.getValue() instanceof Number) {
            final CtTypedElement typedParent = literal.getParent(CtTypedElement.class);
            if (typedParent != null) {// special treatment for int literal
                if (typedParent instanceof CtAbstractInvocation) {
                    final CtExecutableReference<?> executable = ((CtAbstractInvocation) typedParent).getExecutable();
                    final int indexOf = ((CtAbstractInvocation) typedParent).getArguments().indexOf(literal);
                    final CtTypeReference<?> ctTypeReference = executable.getParameters().get(indexOf);
                    if (Number.class.isAssignableFrom(ctTypeReference.getActualClass())) {
                        return ctTypeReference;
                    } else {
                        return literal.getType();
                    }
                } else if (typedParent.getType() instanceof CtArrayTypeReference) {
                    return ((CtArrayTypeReference) typedParent.getType()).getComponentType();
                } else {
                    return typedParent.getType();
                }
            } else {
                throw new IllegalArgumentException(literal.toString());
            }
        } else {
            return literal.getType();
        }
    }

}
