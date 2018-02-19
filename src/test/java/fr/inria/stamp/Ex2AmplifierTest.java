package fr.inria.stamp;

import fr.inria.stamp.ex2amplifier.Ex2Amplifier;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/01/18
 */
public class Ex2AmplifierTest extends AbstractTest {

    @Override
    protected String getPathToConfigurationFile() {
        return "src/test/resources/tavern/tavern.properties";
    }

    @Test
    public void test() throws Exception {

        /*
            Test that the Ex2Amplifier returns a List of CtMethod build thank to CATG.
            Amplified CtMethod has the same "structural" inputs of the test, but with
            different test data input, i.e. literals has been modified.
         */

        final Ex2Amplifier ex2Amplifier = new Ex2Amplifier();
        ex2Amplifier.init(this.configuration);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        ex2Amplifier.reset(testClass);
        final CtMethod<?> test = testClass
                .getMethodsByName("test")
                .get(0);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        assertEquals(3, apply.size());
        System.out.println(apply);
    }

    @Test
    public void testOnNonAmplifiableTestMethod() throws Exception {

        /*
            In Case of the test method does not contain any literal or only null,
            Ex2Amplifier returns an empty list, i.e. it cannot amplifiy such test methods.
         */

        final Ex2Amplifier ex2Amplifier = new Ex2Amplifier();
        ex2Amplifier.init(this.configuration);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        ex2Amplifier.reset(testClass);
        final CtMethod<?> test = testClass
                .getMethodsByName("test2")
                .get(0);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        assertTrue(apply.isEmpty());
    }

    @Test
    public void testUsingJBSE() throws Exception {
        /*
        Test that the Ex2Amplifier returns a List of CtMethod build thank to CATG.
                Amplified CtMethod has the same "structural" inputs of the test, but with
        different test data input, i.e. literals has been modified.
         */

        final Ex2Amplifier ex2Amplifier = new Ex2Amplifier();
        ex2Amplifier.init(this.configuration, Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        ex2Amplifier.reset(testClass);
        final CtMethod<?> test = testClass
                .getMethodsByName("test")
                .get(0);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        System.out.println(apply);
    }

    @Test
    public void testUsingJBSE2() throws Exception {
        final Ex2Amplifier ex2Amplifier = new Ex2Amplifier();
        ex2Amplifier.init(this.configuration, Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        ex2Amplifier.reset(testClass);
        final CtMethod<?> test = testClass
                .getMethodsByName("test2")
                .get(0);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        System.out.println(apply);
    }

    @Ignore
    @Test
    public void testUsingJBSE3() throws Exception {
        final Ex2Amplifier ex2Amplifier = new Ex2Amplifier();
        ex2Amplifier.init(this.configuration, Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        final CtMethod<?> test = testClass
                .getMethodsByName("test3")
                .get(0);
        ex2Amplifier.reset(testClass);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        System.out.println(apply);
    }
}
