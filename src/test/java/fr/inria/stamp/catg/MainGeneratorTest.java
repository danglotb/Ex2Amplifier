package fr.inria.stamp.catg;

import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class MainGeneratorTest extends AbstractTest {

    @Override
    protected String getPathToConfigurationFile() {
        return "src/test/resources/tavern/tavern.properties";
    }

    @Test
    public void test() throws Exception {

        /*
            The MainGenerator should return a main method (std java) build from the given test case.
            The returned main method is the same as the test case, but literals has been extracted has local variables
            and initialize with a method call from CATG: catg.CATG.makeXXX(<originalValue>)
         */

        final CtMethod<?> mainMethodFromTestMethod = MainGenerator.generateMainMethodFromTestMethod(
                this.launcher.getFactory()
                        .Class()
                        .get("fr.inria.stamp.MainTest")
                        .getMethodsByName("test")
                        .get(0)
        );

        System.out.println(mainMethodFromTestMethod);
    }
}
