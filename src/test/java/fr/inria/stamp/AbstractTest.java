package fr.inria.stamp;

import fr.inria.stamp.alloy.builder.ModelBuilder;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.instrumentation.TestInstrumentation;
import org.junit.Before;

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
}
