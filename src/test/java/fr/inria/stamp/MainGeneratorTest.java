package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.AbstractTest;
import fr.inria.stamp.MainGenerator;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertEquals;

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
        final String expectedMainMethod = "public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit1 = catg.CATG.readInt(100);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit2 = catg.CATG.readString(\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit3 = catg.CATG.readInt(5);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit4 = catg.CATG.readString(\"Timoleon\");" + AmplificationHelper.LINE_SEPARATOR +
                "    int lit5 = catg.CATG.readInt(1000);" + AmplificationHelper.LINE_SEPARATOR +
                "    String lit6 = catg.CATG.readString(\"Potion\");" + AmplificationHelper.LINE_SEPARATOR +
                "    Seller seller = new Seller(lit1, Collections.singletonList(new Item(lit2, lit3)));" + AmplificationHelper.LINE_SEPARATOR +
                "    Player player = new Player(lit4, lit5);" + AmplificationHelper.LINE_SEPARATOR +
                "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    player.buyItem(lit6, seller);" + AmplificationHelper.LINE_SEPARATOR +
                "    player.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "    seller.toString();" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMainMethod, mainMethodFromTestMethod.toString());
    }
}
