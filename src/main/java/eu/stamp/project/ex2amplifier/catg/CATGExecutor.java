package eu.stamp.project.ex2amplifier.catg;

import fr.inria.diversify.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class CATGExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CATGExecutor.class);

    public static int maxIterations = 100;

    public static String java_home = "";

    public static List<List<String>> execute(String classpath,
                                             String fullQualifiedNameOfTestClass) {
        CATGUtils.eraseOldFiles();
        int iteration = 1;
        while (iteration <= maxIterations) {
            CATGUtils.copyIfNeeded(iteration);
            try {
                final String command = java_home + CATGUtils.COMMAND_LINE +
                        classpath + CATG_JARS +
                        " -ea " + fullQualifiedNameOfTestClass;
                LOGGER.info("Running CATG: {}", command);
                final Process process = Runtime.getRuntime().exec(command);
                new ThreadToReadInputStream(System.out, process.getInputStream()).start();
                new ThreadToReadInputStream(System.err, process.getErrorStream()).start();
                process.waitFor();
            } catch (Exception ignored) {
                // ignored
            }
            if ((!new File("history").exists()) && (!new File("backtrackFlag").exists())) {
                break;
            } else {
                iteration++;
            }
        }
        return CATGUtils.readOutPut();
    }

    private static final String CATG_JARS =
            AmplificationHelper.PATH_SEPARATOR + "lib/catg-dev.jar" +
            AmplificationHelper.PATH_SEPARATOR + "lib/asm-all-5.0.4.jar" +
            AmplificationHelper.PATH_SEPARATOR + "lib/automaton-1.11-8.jar";

}
