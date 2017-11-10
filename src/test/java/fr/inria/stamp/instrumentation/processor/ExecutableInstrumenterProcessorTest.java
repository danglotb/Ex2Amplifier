package fr.inria.stamp.instrumentation.processor;

import fr.inria.stamp.AbstractTest;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/17
 */
public class ExecutableInstrumenterProcessorTest  extends AbstractTest {

    @Test
    public void testProcess() throws Exception {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("src/test/resources/calculator/src/main/java/fr/inria/calculator/Calculator.java");
        launcher.addInputResource("src/main/java/fr/inria/stamp/alloy/");
        launcher.buildModel();
        launcher.addProcessor(new ExecutableInstrumenterProcessor());
        launcher.process();
        final CtClass<?> classToBeInstrumented = launcher.getFactory().Class().get("fr.inria.calculator.Calculator");
        assertEquals(expectedInstrumentedClass, classToBeInstrumented.toString());
    }

    private static final String expectedInstrumentedClass = "public class Calculator {" + nl +
            "    private int currentValue;" + nl +
            "" + nl +
            "    public Calculator(int value) {" + nl +
            "        ModelBuilder.depopParameters(new fr.inria.stamp.alloy.model.Variable(\"value\", \"Int\"));" + nl +
            "        this.currentValue = value;" + nl +
            "    }" + nl +
            "" + nl +
            "    public void accumulate(int value) {" + nl +
            "        ModelBuilder.depopParameters(new fr.inria.stamp.alloy.model.Variable(\"value\", \"Int\"));" + nl +
            "        if (((this.currentValue) % 3) == 0) {" + nl +
            "            this.currentValue += value;" + nl +
            "        }else {" + nl +
            "            this.currentValue += 2 * value;" + nl +
            "        }" + nl +
            "    }" + nl +
            "" + nl +
            "    public void reset() {" + nl +
            "        if (((this.currentValue) % 5) == 0) {" + nl +
            "            if (((this.currentValue) % 2) == 0) {" + nl +
            "                this.currentValue = 0;" + nl +
            "            }" + nl +
            "        }else {" + nl +
            "            if ((((this.currentValue) * 7) % 23) == 0) {" + nl +
            "                this.currentValue += 21;" + nl +
            "            }" + nl +
            "        }" + nl +
            "    }" + nl +
            "" + nl +
            "    public int aggregate(Calculator calculator) {" + nl +
            "        this.accumulate(calculator.currentValue);" + nl +
            "        calculator.reset();" + nl +
            "        calculator.accumulate(this.currentValue);" + nl +
            "        this.reset();" + nl +
            "        return this.currentValue;" + nl +
            "    }" + nl +
            "" + nl +
            "    public int getCurrentValue() {" + nl +
            "        return currentValue;" + nl +
            "    }" + nl +
            "}";

}
