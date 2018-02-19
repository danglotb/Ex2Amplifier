package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.jbse.ArgumentsExtractor;
import fr.inria.stamp.catg.MainGenerator;
import fr.inria.stamp.catg.CATGExecutor;
import fr.inria.stamp.catg.CATGUtils;
import fr.inria.stamp.jbse.JBSERunner;
import fr.inria.stamp.smt.SMTSolver;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    private Ex2Amplifier_Mode mode = Ex2Amplifier_Mode.CATG;

    public static final TypeFilter<CtLiteral<?>> CT_LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<?>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<?> element) {
            return super.matches(element) && !"<nulltype>".equals(element.getType().getSimpleName());
        }
    };


    public Ex2Amplifier(InputConfiguration configuration, Ex2Amplifier_Mode mode) {
        this.mode = mode;
        if (mode == Ex2Amplifier_Mode.CATG) {
            this.amplifier = new CATGAmplifier(configuration);
        } else {
            this.amplifier = new JBSEAmplifier(configuration);
        }
    }

    public Ex2Amplifier(InputConfiguration configuration) {
        this(configuration, Ex2Amplifier_Mode.CATG);
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
