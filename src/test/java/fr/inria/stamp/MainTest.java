package fr.inria.stamp;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static fr.inria.stamp.AbstractTest.nl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/11/17
 */
public class MainTest {

    @Test
    public void test() throws Exception {

        /*
            test the procedure of DSpot using the Ex2Amplifier
         */

        Main.main(new String[]{
                "-p", "src/test/resources/calculator/calculator.properties",
                "-i", "1",
                "--verbose",
                "-t", "fr.inria.calculator.CalculatorTest",
                "-c", "testAccumulate",
                "-s", "JacocoCoverageSelector",
                "-o", "target/trash"
        });


        assertTrue(new File("target/trash/fr.inria.calculator.CalculatorTest_jacoco_instr_coverage_report.txt").exists());

        // TODO !! FIXME !!
       /* final String expectedReport = "" + nl +
                "======= REPORT =======" + nl +
                "Initial instruction coverage: 67 / 73" + nl +
                "91.78%" + nl +
                "Amplification results with 1 amplified tests." + nl +
                "Amplified instruction coverage: 67 / 73" + nl +
                "91.78%";

        assertEquals(expectedReport,
                Files.readAllLines(Paths.get("target/trash/fr.inria.calculator.CalculatorTest_jacoco_instr_coverage_report.txt")).stream().collect(Collectors.joining(nl)));*/
    }
}
