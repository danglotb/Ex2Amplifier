package eu.stamp_project.ex2amplifier.jbse;

import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class ArgumentsExtractorTest {

    @Test
    public void testExtraction() throws Exception {

        /*
            Test that we can extract all literals, e.g. involved in binary operation
         */

        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.addInputResource("src/test/resources/calculator/src/test/java/fr/inria/calculator/CalculatorTest.java");
        launcher.addInputResource("src/test/resources/calculator/src/main/java/fr/inria/calculator/Calculator.java");
        launcher.buildModel();

        final CtClass<Object> testClass = launcher.getFactory()
                .Class()
                .get("fr.inria.calculator.CalculatorTest");
        final CtMethod<?> test = testClass
                .getMethodsByName("testWithBinaryOnLiteral")
                .get(0);
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(test, testClass);
        System.out.println(extractedMethod);
    }

    @Test
    public void testPerformExtraction() throws Exception {

        /*
            The ArgumentsExtractor should produce a new CtMethod<?>,
            that is semantically equivalent to the test given as input,
            but without assertion, and all used (and supported by JBSE)
            values (literals) will be pass as argument.
            This new CtMethod<?> is used as input for JBSE, and the symbolic execution.
         */

        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.addInputResource("src/test/resources/calculator/src/test/java/fr/inria/calculator/CalculatorTest.java");
        launcher.addInputResource("src/test/resources/calculator/src/main/java/fr/inria/calculator/Calculator.java");
        launcher.buildModel();

        final CtClass<Object> testClass = launcher.getFactory()
                .Class()
                .get("fr.inria.calculator.CalculatorTest");
        final CtMethod<?> test = testClass
                .getMethodsByName("testAccumulate")
                .get(0);
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(test, testClass);

        // same as the MainGenerator, the resulting method from ArgumentsExtractor should be compilable
        testClass.addMethod(extractedMethod);
        launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        assertEquals(expectedMethod, extractedMethod.toString());
        assertNotEquals(test.toString(), extractedMethod.toString());
    }

    private static final String expectedMethod = "public void extract_testAccumulate(int lit0, int lit1) throws Exception {\n" +
            "    try {\n" +
            "        this.setUp();\n" +
            "    } catch (Exception __exceptionEx2Amplifier) {\n" +
            "        throw new RuntimeException(__exceptionEx2Amplifier);\n" +
            "    }\n" +
            "    final Calculator calculator1 = new Calculator(lit0);\n" +
            "    calculator1.getCurrentValue();\n" +
            "    calculator1.accumulate(lit1);\n" +
            "    calculator1.getCurrentValue();\n" +
            "    try {\n" +
            "        this.tearDown();\n" +
            "    } catch (Exception __exceptionEx2Amplifier) {\n" +
            "        throw new RuntimeException(__exceptionEx2Amplifier);\n" +
            "    }\n" +
            "}";


    @Test
    public void testOnTavern() throws Exception {

        /*
            Test the ArgumentsExtractor on the tavern project
         */

        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.addInputResource("src/test/resources/tavern/src/test/java/fr/inria/stamp/MainTest.java");
        launcher.addInputResource("src/test/resources/tavern/src/main/java/fr/inria/stamp/tavern/Seller.java");
        launcher.addInputResource("src/test/resources/tavern/src/main/java/fr/inria/stamp/tavern/Player.java");
        launcher.addInputResource("src/test/resources/tavern/src/main/java/fr/inria/stamp/tavern/Item.java");
        launcher.buildModel();

        final CtClass<Object> testClass = launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        final CtMethod<?> test = testClass
                .getMethodsByName("test")
                .get(0);
        final CtMethod<?> extractedMethod = ArgumentsExtractor.performExtraction(test, testClass);

        System.out.println(extractedMethod);

        // same as the MainGenerator, the resulting method from ArgumentsExtractor should be compilable
        testClass.addMethod(extractedMethod);
        launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        assertEquals(expectMethodTavern, extractedMethod.toString());
        assertNotEquals(test.toString(), extractedMethod.toString());
    }

    private final String expectMethodTavern = "public void extract_test(boolean lit0, char lit1, char lit2, byte lit3, short lit4, int lit5, long lit6, byte lit7, int lit8, int lit9, int lit10) throws Exception {" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        MainTest.setUpBeforeClass();" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        this.setUp();" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(this.aUsedNumber);" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(this.getANumber());" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(\"\\\"bar\\\"\");" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(\"NEW\\nLINE\");" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(lit0);" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(lit1);" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.println(lit2);" + AmplificationHelper.LINE_SEPARATOR +
            "    byte b = lit3;" + AmplificationHelper.LINE_SEPARATOR +
            "    short s = lit4;" + AmplificationHelper.LINE_SEPARATOR +
            "    int i = lit5;" + AmplificationHelper.LINE_SEPARATOR +
            "    long l = lit6;" + AmplificationHelper.LINE_SEPARATOR +
            "    byte[] array_byte = new byte[]{ lit7 };" + AmplificationHelper.LINE_SEPARATOR +
            "    Integer toto = null;" + AmplificationHelper.LINE_SEPARATOR +
            "    Seller seller = new Seller(lit8, Collections.singletonList(new Item(\"Potion\", lit9)));" + AmplificationHelper.LINE_SEPARATOR +
            "    Player player = new Player(\"Timoleon\", lit10);" + AmplificationHelper.LINE_SEPARATOR +
            "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
            "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
            "    player.buyItem(\"Potion\", seller);" + AmplificationHelper.LINE_SEPARATOR +
            "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
            "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        this.tearDown();" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        MainTest.tearDownAfterClass();" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}";
}
