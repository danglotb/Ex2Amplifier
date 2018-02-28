package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.ex2amplifier.Ex2Amplifier;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

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

        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.CATG);
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
        try (BufferedReader buffer = new BufferedReader(new FileReader(
                this.configuration.getOutputDirectory() + "/" +
                        testClass.getSimpleName() + "/test_values.csv"))) {
            assertEquals("\"\\\"bar\\\"\",\"\",\"\",\"\"" + AmplificationHelper.LINE_SEPARATOR +
                            "\"NEW\\nLINE\",\"\",\"\",\"\"" + AmplificationHelper.LINE_SEPARATOR +
                            "true,false,false,false" + AmplificationHelper.LINE_SEPARATOR +
                            "'<','0','0','0'" + AmplificationHelper.LINE_SEPARATOR +
                            "'\\'','0','0','0'" + AmplificationHelper.LINE_SEPARATOR +
                            "3,0,0,0,0,0,0,0,0,0,0,0,0" + AmplificationHelper.LINE_SEPARATOR +
                            "16,0,0,0" + AmplificationHelper.LINE_SEPARATOR +
                            "100,0,0,0" + AmplificationHelper.LINE_SEPARATOR +
                            "\"Potion\",\"\\uffff\",\"\\u0000\",\"\",\"\",\"\",\"\"" + AmplificationHelper.LINE_SEPARATOR +
                            "5,0,0,1" + AmplificationHelper.LINE_SEPARATOR +
                            "\"Timoleon\",\"\",\"\",\"\"" + AmplificationHelper.LINE_SEPARATOR +
                            "1000,0,0,0",
                    buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(apply);
    }

    @Test
    public void testOnNonAmplifiableTestMethod() throws Exception {

        /*
            In Case of the test method does not contain any literal or only null,
            Ex2Amplifier returns an empty list, i.e. it cannot amplifiy such test methods.
         */

        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.CATG);
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

        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        ex2Amplifier.init(this.configuration);
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
        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        ex2Amplifier.init(this.configuration);
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
        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        ex2Amplifier.init(this.configuration);
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
