package eu.stamp_project.ex2amplifier.catg;

import eu.stamp_project.utils.AmplificationHelper;
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

    public static File workingDirectory = null;

    public static String prefixWorkingDirectory = "";

    public static int maxIterations = 100;

    public static String java_home = "";

    public static List<List<String>> execute(String classpath,
                                             String fullQualifiedNameOfTestClass) {
        CATGUtils.eraseOldFiles();
        int iteration = 1;
        while (iteration <= maxIterations) {
            CATGUtils.copyIfNeeded(iteration);
            try {
                final String command = java_home +
                        CATGUtils.COMMAND_LINE +
                        CATGUtils.CONF_FILE_OPT + new File(CATGUtils.CONF_FILE_NAME).getAbsolutePath() +
                        CATGUtils.AGENT_OPT + new File(CATGUtils.AGENT_FILE_NAME).getAbsolutePath() +
                        " -cp " +  classpath + CATG_JARS +
                        " -ea " + fullQualifiedNameOfTestClass;
                LOGGER.info("Running CATG: {}", command);
                final Process process = Runtime.getRuntime().exec(
                        command,
                        null,
                        workingDirectory
                );
                new ThreadToReadInputStream(System.out, process.getInputStream()).start();
                new ThreadToReadInputStream(System.err, process.getErrorStream()).start();
                process.waitFor();
            } catch (Exception ignored) {
                // ignored
            }
            if ((!new File(prefixWorkingDirectory + "history").exists()) && (!new File(prefixWorkingDirectory + "backtrackFlag").exists())) {
                break;
            } else {
                iteration++;
            }
        }
        return CATGUtils.readOutPut();
    }

    public static void setWorkingDirectory(File workingDirectory) {
        CATGExecutor.workingDirectory = workingDirectory;
        if (workingDirectory != null) {
            prefixWorkingDirectory = workingDirectory.getAbsolutePath() + "/";
        } else {
            prefixWorkingDirectory = "";
        }
    }

    public static String getPrefixWorkingDirectory() {
        return prefixWorkingDirectory;
    }

    private static final String CATG_JARS =
            AmplificationHelper.PATH_SEPARATOR + new File("lib/catg-dev.jar").getAbsolutePath() +
            AmplificationHelper.PATH_SEPARATOR + new File("lib/asm-all-5.0.4.jar").getAbsolutePath() +
            AmplificationHelper.PATH_SEPARATOR + new File("lib/automaton-1.11-8.jar").getAbsolutePath();

}
