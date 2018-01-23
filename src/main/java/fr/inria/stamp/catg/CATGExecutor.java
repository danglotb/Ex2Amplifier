package fr.inria.stamp.catg;

import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class CATGExecutor {

    public static int maxIterations = 100;

    public static List<List<CtLiteral<?>>> execute(Factory factory,
                                                   String classpath,
                                                   String fullQualifiedNameOfTestClass) {

        CATGUtils.eraseOldFiles();
        int iteration = 1;
        while (iteration <= maxIterations) {
            CATGUtils.copyIfNeeded(iteration);
            try {
                final String command = CATGUtils.COMMAND_LINE +
                        classpath + CATG_JARS +
                        " -ea " + fullQualifiedNameOfTestClass;
                System.out.println(command);
                final Process process = Runtime.getRuntime().exec(command);
                new ThreadToReadInputStream(System.out, process.getInputStream()).start();
                new ThreadToReadInputStream(System.err, process.getErrorStream()).start();
                process.waitFor();
            } catch (Exception ignored) {
                // ignored
            }
            if (!new File("hisory").exists() && !new File("backtrackFlag").exists()) {
                break;
            } else {
                iteration++;
            }
        }

        return CATGUtils.readOutPut(factory);
    }

    private static final String CATG_JARS =
            AmplificationHelper.PATH_SEPARATOR + "lib/catg-dev.jar" +
            AmplificationHelper.PATH_SEPARATOR + "lib/asm-all-5.0.4.jar" +
            AmplificationHelper.PATH_SEPARATOR + "lib/automaton-1.11-8.jar";

}
