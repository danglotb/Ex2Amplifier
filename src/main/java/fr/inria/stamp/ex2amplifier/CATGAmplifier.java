package fr.inria.stamp.ex2amplifier;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.catg.CATGExecutor;
import fr.inria.stamp.catg.CATGUtils;
import fr.inria.stamp.catg.MainGenerator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/02/18
 */
class CATGAmplifier implements Amplifier {

    private InputConfiguration configuration;

    CATGAmplifier(InputConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CtMethod> apply(CtMethod ctMethod) {
        final CtMethod<?> mainMethodFromTestMethod =
                MainGenerator.generateMainMethodFromTestMethod(ctMethod);
        final CtClass testClass = ctMethod.getParent(CtClass.class);
        testClass.addMethod(mainMethodFromTestMethod);
        DSpotUtils.printJavaFileWithComment(testClass, new File("target/dspot/tmp_test_sources"));
        String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(this.configuration.getInputProgram().getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/" + this.configuration.getInputProgram().getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getInputProgram().getProgramDir() + "/" + this.configuration.getInputProgram().getTestClassesDir();
        DSpotCompiler.compile("target/dspot/tmp_test_sources", classpath + AmplificationHelper.PATH_SEPARATOR + "lib/catg-dev.jar",
                new File(this.configuration.getInputProgram().getProgramDir() + "/" +
                        this.configuration.getInputProgram().getTestClassesDir())
        );
        final List<List<String>> execute = CATGExecutor.execute(classpath, testClass.getQualifiedName());
        CATGUtils.eraseOldFiles();
        testClass.removeMethod(mainMethodFromTestMethod);
        return execute.stream()
                .map(values -> buildMethodFromValues(values, ctMethod))
                .collect(Collectors.toList());
    }

    private CtMethod<?> buildMethodFromValues(List<String> values, CtMethod originalTestMethod) {
        final Iterator<String> iteratorOnNewValues = values.iterator();
        final CtMethod<?> clone = new AssertionRemover().removeAssertion(
                AmplificationHelper.cloneMethodTest(originalTestMethod, "Ex2Amplifier")
        );
        final List<CtLiteral<?>> originalLiterals = clone.getBody().getElements(Ex2Amplifier.CT_LITERAL_TYPE_FILTER);
        originalLiterals.forEach(ctLiteral ->
                ctLiteral.replace(buildNewLiteralFromString(iteratorOnNewValues.next(), ctLiteral))
        );
        return clone;
    }

    private CtLiteral<?> buildNewLiteralFromString(String value, CtLiteral<?> originalLiteral) {
        final Object originalLiteralValue = originalLiteral.getValue();
        final Factory factory = originalLiteral.getFactory();
        if (originalLiteralValue instanceof String) {
            return factory.createLiteral(value);
        } else if (originalLiteralValue instanceof Integer) {
            return factory.createLiteral(Integer.parseInt(value));
        } else if (originalLiteralValue instanceof Boolean) {
            return factory.createLiteral(value.equals("true"));
        } else if (originalLiteralValue instanceof Character) {
            return factory.createLiteral(value.charAt(0));
        } else {
            throw new UnsupportedOperationException(originalLiteralValue.getClass() + " is not supported");
        }
    }

    @Override
    public void reset(CtType ctType) {

    }
}
