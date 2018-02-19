package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/01/18
 */
public class Ex2Amplifier implements Amplifier {

    public enum Ex2Amplifier_Mode {
            CATG,
            JBSE
    }

    private Amplifier amplifier;

    public static final TypeFilter<CtLiteral<?>> CT_LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<?>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<?> element) {
            return super.matches(element) && !"<nulltype>".equals(element.getType().getSimpleName());
        }
    };

    public void init(InputConfiguration configuration) {
        this.init(configuration, Ex2Amplifier_Mode.CATG);
    }

    public void init(InputConfiguration configuration, Ex2Amplifier_Mode mode) {
        if (mode == Ex2Amplifier_Mode.CATG) {
            this.amplifier = new CATGAmplifier(configuration);
        } else {
            this.amplifier = new JBSEAmplifier(configuration);
        }
    }

    @Override
    public List<CtMethod> apply(CtMethod ctMethod) {
        if (ctMethod.getElements(CT_LITERAL_TYPE_FILTER).isEmpty()) {
            return Collections.emptyList();
        }
        return this.amplifier.apply(ctMethod);
    }

    @Override
    public void reset(CtType ctType) {
       this.amplifier.reset(ctType);
    }
}
