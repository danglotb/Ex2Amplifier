package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.AbstractTest;
import fr.inria.stamp.MainGenerator;
import org.junit.Test;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

            The produced main method must be compilable and runnable
         */

        final CtClass<Object> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.stamp.MainTest");
        final CtMethod<?> mainMethodFromTestMethod = MainGenerator.generateMainMethodFromTestMethod(
                testClass.getMethodsByName("test").get(0)
        );
        final String expectedMainMethod = "public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR +
                "    MainTest mainTest = new MainTest();" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        mainTest.setUp();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit1 = catg.CATG.readInt(100);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit2 = catg.CATG.readString(\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit3 = catg.CATG.readInt(5);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit4 = catg.CATG.readString(\"Timoleon\");" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit5 = catg.CATG.readInt(1000);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit6 = catg.CATG.readString(\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "    System.out.println(mainTest.aUsedNumber);" + AmplificationHelper.LINE_SEPARATOR +
                "    System.out.println(mainTest.getANumber());" + AmplificationHelper.LINE_SEPARATOR +
                "    Seller seller = new Seller(lit1, Collections.singletonList(new Item(lit2, lit3)));" + AmplificationHelper.LINE_SEPARATOR +
                "    Player player = new Player(lit4, lit5);" + AmplificationHelper.LINE_SEPARATOR +
                "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    player.buyItem(lit6, seller);" + AmplificationHelper.LINE_SEPARATOR +
                "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        mainTest.tearDown();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMainMethod, mainMethodFromTestMethod.toString());

        testClass.addMethod(mainMethodFromTestMethod);
        this.launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        this.launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);
    }
}
