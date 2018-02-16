package fr.inria.stamp.catg;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.AbstractTest;
import fr.inria.stamp.catg.MainGenerator;
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

        final CtClass<Object> testClass = this.launcher.getFactory().Class().get("fr.inria.stamp.MainTest");
        final CtMethod<?> mainMethodFromTestMethod = MainGenerator.generateMainMethodFromTestMethod(
                testClass.getMethodsByName("test").get(0)
        );

        System.out.println(mainMethodFromTestMethod.toString());
        testClass.addMethod(mainMethodFromTestMethod);
        this.launcher.getModelBuilder().setBinaryOutputDirectory(new File("target/trash/"));
        this.launcher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

        final String expectedMainMethod = "public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR +
                "    MainTest mainTest = new MainTest();" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        mainTest.setUp();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        String lit1 = catg.CATG.readString((String)\"\\\"bar\\\"\");" + AmplificationHelper.LINE_SEPARATOR +
                "        String lit2 = catg.CATG.readString((String)\"NEW\" + System.getProperty(\"line.separator\") + \"LINE\");" + AmplificationHelper.LINE_SEPARATOR +
                "        boolean lit3 = catg.CATG.readBool((boolean)true);" + AmplificationHelper.LINE_SEPARATOR +
                "        char lit4 = catg.CATG.readChar((char)'<');" + AmplificationHelper.LINE_SEPARATOR +
                "        char lit5 = catg.CATG.readChar((char)'\\'');" + AmplificationHelper.LINE_SEPARATOR +
                "        byte lit6 = catg.CATG.readByte((byte)3);" + AmplificationHelper.LINE_SEPARATOR +
                "        short lit7 = catg.CATG.readShort((short)3);" + AmplificationHelper.LINE_SEPARATOR +
                "        int lit8 = catg.CATG.readInt((int)3);" + AmplificationHelper.LINE_SEPARATOR +
                "        long lit9 = catg.CATG.readLong((long)3);" + AmplificationHelper.LINE_SEPARATOR +
                "        int lit10 = catg.CATG.readInt((int)100);" + AmplificationHelper.LINE_SEPARATOR +
                "        String lit11 = catg.CATG.readString((String)\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "        int lit12 = catg.CATG.readInt((int)5);" + AmplificationHelper.LINE_SEPARATOR +
                "        String lit13 = catg.CATG.readString((String)\"Timoleon\");" + AmplificationHelper.LINE_SEPARATOR +
                "        int lit14 = catg.CATG.readInt((int)1000);" + AmplificationHelper.LINE_SEPARATOR +
                "        String lit15 = catg.CATG.readString((String)\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(mainTest.aUsedNumber);" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(mainTest.getANumber());" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(lit1);" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(lit2);" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(lit3);" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(lit4);" + AmplificationHelper.LINE_SEPARATOR +
                "        System.out.println(lit5);" + AmplificationHelper.LINE_SEPARATOR +
                "        byte b = lit6;" + AmplificationHelper.LINE_SEPARATOR +
                "        short s = lit7;" + AmplificationHelper.LINE_SEPARATOR +
                "        int i = lit8;" + AmplificationHelper.LINE_SEPARATOR +
                "        long l = lit9;" + AmplificationHelper.LINE_SEPARATOR +
                "        Seller seller = new Seller(lit10, Collections.singletonList(new Item(lit11, lit12)));" + AmplificationHelper.LINE_SEPARATOR +
                "        Player player = new Player(lit13, lit14);" + AmplificationHelper.LINE_SEPARATOR +
                "        player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "        seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "        player.buyItem(lit15, seller);" + AmplificationHelper.LINE_SEPARATOR +
                "        player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "        seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2AmplifierException) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2AmplifierException);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "    try {" + AmplificationHelper.LINE_SEPARATOR +
                "        mainTest.tearDown();" + AmplificationHelper.LINE_SEPARATOR +
                "    } catch (Exception __exceptionEx2Amplifier) {" + AmplificationHelper.LINE_SEPARATOR +
                "        throw new RuntimeException(__exceptionEx2Amplifier);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMainMethod, mainMethodFromTestMethod.toString());
    }
}
