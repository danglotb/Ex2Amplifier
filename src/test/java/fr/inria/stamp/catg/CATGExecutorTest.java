package fr.inria.stamp.catg;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class CATGExecutorTest extends AbstractTest {

    @Override
    protected String getPathToConfigurationFile() {
        return "src/test/resources/tavern/tavern.properties";
    }

    @Test
    public void test() throws Exception {

        /*
            Test that CATGExecutor returns a List of List of Literals that explore the program.
            The input is TestClass with a main method generated with MainGenerator
         */


        final String qualifiedName = "fr.inria.stamp.MainTest";
        final CtClass<Object> testClass = this.launcher.getFactory()
                .Class()
                .get(qualifiedName);
        final CtMethod<?> mainMethodFromTestMethod = MainGenerator.generateMainMethodFromTestMethod(
                testClass.getMethodsByName("test")
                        .get(0)
        );
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

        final List<List<CtLiteral<?>>> execute = CATGExecutor.execute(this.launcher.getFactory(),
                classpath,
                qualifiedName
        );
        System.out.println(execute);
    }
}
