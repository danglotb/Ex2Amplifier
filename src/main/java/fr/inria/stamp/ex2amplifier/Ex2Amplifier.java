package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
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

    public static final TypeFilter<CtLiteral<?>> CT_LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<?>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<?> element) {
            return super.matches(element) && !"<nulltype>".equals(element.getType().getSimpleName());
        }
    };

    private InputConfiguration configuration;

    public void init(InputConfiguration configuration) {
        this.init(configuration, Ex2Amplifier_Mode.CATG);
    }

    public void init(InputConfiguration configuration, Ex2Amplifier_Mode mode) {
        this.configuration = configuration;
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
        final List<CtMethod> amplifiedTests = this.amplifier.apply(ctMethod);
        if (!amplifiedTests.isEmpty()) {
            this.printAmplifiedTest(amplifiedTests);
        }
        return amplifiedTests;
    }

    // this output is meant to be used in the manual analysis of the result
    // in order to understand how it succeed or why the Ex2Amplifier is failing to catch the change behavior
    private void printAmplifiedTest(List<CtMethod> amplifiedTests) {
        try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() + "amplified_tests.txt", true)) {
            writer.write(amplifiedTests.stream()
                    .map(CtMethod::toString)
                    .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset(CtType ctType) {
       this.amplifier.reset(ctType);
    }
}
