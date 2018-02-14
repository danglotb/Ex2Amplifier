package fr.inria.stamp.jbse;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/02/18
 */
public class JBSERunnterTest2 extends AbstractTest {

    @Override
    protected String getPathToConfigurationFile() {
        return "src/test/resources/tavern/tavern.properties";
    }

    @Test
    public void test() throws Exception {
        final InputProgram program = configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtClass<?> mainClass = this.launcher.getFactory().Class()
                .get("fr.inria.stamp.MainTest");
        final CtMethod<?> testMethod = mainClass
                .getMethodsByName("test3")
                .get(0);

        final CtMethod<?> ctMethod = ArgumentsExtractor.performExtraction(testMethod);
        mainClass.addMethod(ctMethod);

        DSpotUtils.printJavaFileWithComment(mainClass, new File("target/dspot/tmp_test_sources"));
        DSpotCompiler.compile("target/dspot/tmp_test_sources", classpath,
                new File(this.configuration.getInputProgram().getProgramDir() + "/" +
                        this.configuration.getInputProgram().getTestClassesDir())
        );

        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, ctMethod);
        System.out.println(conditionOnVariablesForEachState);
    }

}