package fr.inria.stamp.jbse;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.jbse.ArgumentsExtractor;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class ArgumentsExtractorTest {

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
        final CtMethod<?> extractedMethod =
                ArgumentsExtractor.performExtraction(testClass
                        .getMethodsByName("testAccumulate")
                        .get(0)
                );

        // same as the MainGenerator, the resulting method from ArgumentsExtractor should be compilable
        testClass.addMethod(extractedMethod);
        launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        assertEquals(expectedMethod, extractedMethod.toString());
    }

    private static final String expectedMethod = "public void extract_testAccumulate(int lit0, int lit1) throws Exception {" + AmplificationHelper.LINE_SEPARATOR +
            "    final Calculator calculator1 = new Calculator(lit0);" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.getCurrentValue();" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.accumulate(lit1);" + AmplificationHelper.LINE_SEPARATOR +
            "    calculator1.getCurrentValue();" + AmplificationHelper.LINE_SEPARATOR +
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
        final CtMethod<?> extractedMethod =
                ArgumentsExtractor.performExtraction(testClass
                        .getMethodsByName("test")
                        .get(0)
                );

        System.out.println(extractedMethod);

        // same as the MainGenerator, the resulting method from ArgumentsExtractor should be compilable
        testClass.addMethod(extractedMethod);
        launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        assertEquals(expectMethodTavern, extractedMethod.toString());
    }

    private final String expectMethodTavern = "public void extract_test(boolean lit0, char lit1, char lit2, byte lit3, short lit4, int lit5, long lit6, byte lit7, int lit8, int lit9, int lit10) throws Exception {" + AmplificationHelper.LINE_SEPARATOR + 
            "    System.out.println(this.aUsedNumber);" + AmplificationHelper.LINE_SEPARATOR + 
            "    System.out.println(getANumber());" + AmplificationHelper.LINE_SEPARATOR + 
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
            "}";
}
