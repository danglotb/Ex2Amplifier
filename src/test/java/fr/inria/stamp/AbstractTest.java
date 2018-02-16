package fr.inria.stamp;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class AbstractTest {

    protected String getPathToConfigurationFile() {
        return "src/test/resources/calculator/calculator.properties";
    }

    @Before
    public void setUp() throws Exception {
        Main.verbose = true;
        try {
            FileUtils.forceDelete(new File("target/dspot/tmp_test_sources"));
        } catch (Exception ignored) {

        }
        this.launcher = this.initSpoonModel(this.getPathToConfigurationFile());
        AutomaticBuilderFactory.getAutomaticBuilder(this.configuration)
                .compile(this.configuration.getInputProgram().getProgramDir());
    }

    @After
    public void tearDown() throws Exception {
        Main.verbose = false;
    }

    protected Launcher launcher;

    protected InputConfiguration configuration;

    private Launcher initSpoonModel(String pathToConfigurationFile) {
        try {
            this.configuration = new InputConfiguration(pathToConfigurationFile);
            final InputProgram program = InputConfiguration.initInputProgram(this.configuration);
            this.configuration.setInputProgram(program);
            AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration);
            String dependencies = builder.buildClasspath(program.getProgramDir());
            return initLauncher(program.getAbsoluteSourceCodeDir(),
                    program.getAbsoluteTestSourceCodeDir(),
                    dependencies + AmplificationHelper.PATH_SEPARATOR + "lib/catg-dev.jar");
        } catch (IOException ignored) {
            fail();//should not happen
        }
        return null;// make javac happy
    }

    private Launcher initLauncher(String pathToSources, String pathToTestSources, String dependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = (pathToSources + AmplificationHelper.PATH_SEPARATOR
                + pathToTestSources)
                .split(AmplificationHelper.PATH_SEPARATOR);
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        if (!dependencies.isEmpty()) {
            String[] dependenciesArray = dependencies.split(AmplificationHelper.PATH_SEPARATOR);
            launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        }
        launcher.buildModel();
        return launcher;
    }
}
