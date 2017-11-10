package fr.inria.stamp.instrumentation;

import fr.inria.stamp.AbstractTest;
import fr.inria.stamp.instrumentation.processor.ConstraintInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ExecutableInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.InvocationInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ModificationInstrumenterProcessor;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/11/17
 */
public class AllInstrumentation extends AbstractTest {

    @Test
    public void testProcess() throws Exception {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource("src/test/resources/calculator/src/main/java/fr/inria/calculator/Calculator.java");
        launcher.addInputResource("src/main/java/fr/inria/stamp/alloy/");
        launcher.buildModel();
        launcher.addProcessor(new InvocationInstrumenterProcessor());
        launcher.addProcessor(new ExecutableInstrumenterProcessor());
        launcher.addProcessor(new ConstraintInstrumenterProcessor());
        launcher.addProcessor(new ModificationInstrumenterProcessor());
        launcher.process();
        final CtClass<?> classToBeInstrumented = launcher.getFactory().Class().get("fr.inria.calculator.Calculator");
        assertEquals(expectedInstrumentedClass, classToBeInstrumented.toString());
    }

    private static final String expectedInstrumentedClass = "public class Calculator {" + nl +
            "    private int currentValue;" + nl +
            "" + nl +
            "    public Calculator(int value) {" + nl +
            "        ModelBuilder.depopParameters(new Variable(\"value\", \"Int\"));" + nl +
            "        this.currentValue = value;" + nl +
            "        ModelBuilder.addModification(this, \"currentValue\", \"value\", new Variable(\"currentValue\", \"Int\"));" + nl +
            "    }" + nl +
            "" + nl +
            "    public void accumulate(int value) {" + nl +
            "        ModelBuilder.depopParameters(new Variable(\"value\", \"Int\"));" + nl +
            "        if ((((this.currentValue) % 3) == 0)) {" + nl +
            "            ModelBuilder.addConstraint(this, \"rem[currentValue,3]=0\");" + nl +
            "            (this.currentValue) += value;" + nl +
            "            ModelBuilder.addModification(this, \"currentValue\", \"plus[currentValue,value]\", new Variable(\"currentValue\", \"Int\"));" + nl +
            "            ModelBuilder.endConstraint();" + nl +
            "        }else {" + nl +
            "            ModelBuilder.addConstraint(this, \"not rem[currentValue,3]=0\");" + nl +
            "            (this.currentValue) += (2 * value);" + nl +
            "            ModelBuilder.addModification(this, \"currentValue\", \"plus[currentValue,mul[2,value]]\", new Variable(\"currentValue\", \"Int\"));" + nl +
            "            ModelBuilder.endConstraint();" + nl +
            "        }" + nl +
            "    }" + nl +
            "" + nl +
            "    public void reset() {" + nl +
            "        if ((((this.currentValue) % 5) == 0)) {" + nl +
            "            ModelBuilder.addConstraint(this, \"rem[currentValue,5]=0\");" + nl +
            "            if ((((this.currentValue) % 2) == 0)) {" + nl +
            "                ModelBuilder.addConstraint(this, \"rem[currentValue,2]=0\");" + nl +
            "                this.currentValue = 0;" + nl +
            "                ModelBuilder.addModification(this, \"currentValue\", \"0\", new Variable(\"currentValue\", \"Int\"));" + nl +
            "                ModelBuilder.endConstraint();" + nl +
            "            }else {" + nl +
            "                ModelBuilder.addConstraint(this, \"not rem[currentValue,2]=0\");" + nl +
            "                ModelBuilder.endConstraint();" + nl +
            "            }" + nl +
            "            ModelBuilder.endConstraint();" + nl +
            "        }else {" + nl +
            "            ModelBuilder.addConstraint(this, \"not rem[currentValue,5]=0\");" + nl +
            "            if (((((this.currentValue) * 7) % 23) == 0)) {" + nl +
            "                ModelBuilder.addConstraint(this, \"rem[mul[currentValue,7],23]=0\");" + nl +
            "                (this.currentValue) += 21;" + nl +
            "                ModelBuilder.addModification(this, \"currentValue\", \"plus[currentValue,21]\", new Variable(\"currentValue\", \"Int\"));" + nl +
            "                ModelBuilder.endConstraint();" + nl +
            "            }else {" + nl +
            "                ModelBuilder.addConstraint(this, \"not rem[mul[currentValue,7],23]=0\");" + nl +
            "                ModelBuilder.endConstraint();" + nl +
            "            }" + nl +
            "            ModelBuilder.endConstraint();" + nl +
            "        }" + nl +
            "    }" + nl +
            "" + nl +
            "    public int aggregate(Calculator calculator) {" + nl +
            "        ModelBuilder.addParameters(new Variable(\"calculator.currentValue\", \"Int\"));" + nl +
            "        this.accumulate(calculator.currentValue);" + nl +
            "        calculator.reset();" + nl +
            "        ModelBuilder.addParameters(new Variable(\"this.currentValue\", \"Int\"));" + nl +
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
