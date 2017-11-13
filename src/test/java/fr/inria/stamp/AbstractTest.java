package fr.inria.stamp;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.alloy.builder.ModelBuilder;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.instrumentation.TestInstrumentation;
import org.junit.Before;
import spoon.Launcher;

import java.util.Arrays;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class AbstractTest {

    public static final String nl = System.getProperty("line.separator");

    @Before
    public void setUp() throws Exception {
        ModelBuilder.model = new Model();
        TestInstrumentation.index = new int[]{0};
    }

    public static Launcher initLauncher(String pathToSources, String pathToTestSources, String dependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = (pathToSources + AmplificationHelper.PATH_SEPARATOR + pathToTestSources +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/model/" +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/builder/"
        ).split(AmplificationHelper.PATH_SEPARATOR);
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        if (!dependencies.isEmpty()) {
            String[] dependenciesArray = dependencies.split(AmplificationHelper.PATH_SEPARATOR);
            launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        }
        launcher.buildModel();
        return launcher;
    }
}
