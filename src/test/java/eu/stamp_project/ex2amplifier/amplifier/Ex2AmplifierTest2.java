package eu.stamp_project.ex2amplifier.amplifier;

import eu.stamp_project.ex2amplifier.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/03/18
 */
public class Ex2AmplifierTest2 extends AbstractTest {

    // TODO
    @Ignore
    @Test
    public void test() throws Exception {
        final Ex2Amplifier ex2Amplifier = Ex2Amplifier.getEx2Amplifier(Ex2Amplifier.Ex2Amplifier_Mode.JBSE);
        ex2Amplifier.init(this.configuration);
        final CtClass<?> testClass = this.launcher.getFactory()
                .Class()
                .get("fr.inria.calculator.CalculatorTest");
        ex2Amplifier.reset(testClass);
        final CtMethod<?> test = testClass
                .getMethodsByName("testAggregate")
                .get(0);
        final List<CtMethod> apply = ex2Amplifier.apply(test);
        System.out.println(apply);
    }
}
