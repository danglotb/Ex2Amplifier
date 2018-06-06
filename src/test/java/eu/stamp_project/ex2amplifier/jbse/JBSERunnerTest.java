package eu.stamp_project.ex2amplifier.jbse;

import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.ex2amplifier.smt.SMTSolver;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.sosiefier.InputProgram;
import eu.stamp_project.ex2amplifier.AbstractTest;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.stamp_project.ex2amplifier.amplifier.Ex2Amplifier.CT_LITERAL_TYPE_FILTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class JBSERunnerTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            Test the run method of JBSERunner. JBSE should return a List of Map,
            that associates a name of parameter to its constraints to reach each state.
            Parameter are extracted literals from test.
         */

        final InputProgram program = this.configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                this.configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtMethod<?> testMethod = this.launcher.getFactory().Class()
                .get("fr.inria.calculator.CalculatorTest")
                .getMethodsByName("testAccumulateWithParameters")
                .get(0);

        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);
        assertEquals(2, conditionOnVariablesForEachState.size());
        assertEquals("[{param1=[param1 % 3 == 0]}, {param1=[param1 % 3 != 0]}]", conditionOnVariablesForEachState.toString());
    }

    @Test
    public void test2() throws Exception {

        /*
            Same as Test, but on another test method (compareTo).
            In this case, there is also a parameter in the second operand
         */

        final InputProgram program = configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtMethod<?> testMethod = this.launcher.getFactory().Class()
                .get("fr.inria.calculator.CalculatorTest")
                .getMethodsByName("testCompareToWithParameters")
                .get(0);
        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);
        assertEquals("[{param1=[param1 > param2]}, {param1=[param1 <= param2]}]", conditionOnVariablesForEachState.toString());
    }

    @Test
    public void test3() throws Exception {

        /*
            Same as Test, but on another test method (compareTo).
            In this case, there is also a parameter in the second operand
         */

        final InputProgram program = configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtClass<Object> testClass = this.launcher.getFactory().Class().get("fr.inria.calculator.CalculatorTest");
        final CtMethod<?> testMethod = testClass
                .getMethodsByName("extract_testWithBinaryOnLiteral")
                .get(0);

        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);

        System.out.println(conditionOnVariablesForEachState);
    }


    @Test
    public void testCompute() throws Exception {

        /*
            Same as Test, but on another test method (compareTo).
            In this case, there is also a parameter in the second operand
         */

        final InputProgram program = configuration.getInputProgram();
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(
                configuration
        ).buildClasspath(program.getProgramDir()) + ":" +
                program.getProgramDir() + program.getClassesDir() + ":" +
                program.getProgramDir() + program.getTestClassesDir();
        final CtClass<Object> testClass = this.launcher.getFactory().Class().get("fr.inria.calculator.CalculatorTest");
        final CtMethod<?> testMethod = testClass
                .getMethodsByName("extract_testCompute")
                .get(0);


        final CtMethod<?> testCompute = testClass
                .getMethodsByName("testCompute")
                .get(0);

        System.out.println(ArgumentsExtractor.performExtraction(testCompute, testClass));

        final List<Map<String, List<String>>> conditionOnVariablesForEachState =
                JBSERunner.runJBSE(classpath, testMethod);

        System.out.println(conditionOnVariablesForEachState);

        final List<? extends CtMethod<?>> collect = conditionOnVariablesForEachState.stream()
                .filter(condition -> !condition.isEmpty())
                .map(conditions ->
                        this.generateNewTestMethod(testCompute, conditions)
                ).filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println(collect);
    }

    // the parameters list will be given to initialize variables in SMT solver
    private CtMethod<?> generateNewTestMethod(CtMethod<?> testMethod,
                                              Map<String, List<String>> conditionForParameter) {
        final CtMethod clone = AmplificationHelper.cloneTestMethodForAmp(testMethod, "_Ex2_JBSE");
        final List<?> solutions = SMTSolver.solve(conditionForParameter, 3);
        System.out.println(solutions);
        final Iterator<?> iterator = solutions.iterator();
        final List<CtLiteral<?>> originalLiterals = clone.getElements(CT_LITERAL_TYPE_FILTER)
                .stream().filter(literal ->
                        !(literal.getValue() instanceof String)).collect(Collectors.toList()
                );
        System.out.println(originalLiterals);
        originalLiterals.forEach(literalToBeReplaced -> {
            try {
                final CtLiteral<?> newLiteral = testMethod.getFactory().createLiteral(iterator.next());
                if (literalToBeReplaced.getParent() instanceof CtUnaryOperator) {
                    literalToBeReplaced.getParent().replace(newLiteral);
                } else {
                    literalToBeReplaced.replace(newLiteral);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return clone;
    }
}
