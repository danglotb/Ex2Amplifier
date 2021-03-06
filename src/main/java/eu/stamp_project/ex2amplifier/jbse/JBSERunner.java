package eu.stamp_project.ex2amplifier.jbse;

import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.settings.SettingsReader;
import jbse.mem.ClauseAssume;
import jbse.mem.State;
import jbse.val.Expression;
import jbse.val.NarrowingConversion;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.WideningConversion;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/01/18
 */
public class JBSERunner {

    public static int countScope = 1500;

    public static int depthScope = 8;

    public static final Map<String, String> typeToDescriptor =
            new HashMap<>();

    static {
        typeToDescriptor.put("byte", "B");
        typeToDescriptor.put("char", "C");
        typeToDescriptor.put("double", "D");
        typeToDescriptor.put("float", "F");
        typeToDescriptor.put("int", "I");
        typeToDescriptor.put("long", "J");
        typeToDescriptor.put("short", "S");
        typeToDescriptor.put("boolean", "Z");
        //typeToDescriptor.put("String", "Ljava/lang/String;");
    }

    private final static Function<Class, String> GET_PATH_OF_JAR_FROM_CLASS = aClass ->
            aClass.getResource("/" + aClass.getName().replaceAll("\\.", "/") + ".class").getPath().split("!")[0];

    public static List<Map<String, List<String>>> runJBSE(String classpath, CtMethod<?> testMethod) {
        final RunParameters p = new RunParameters();
        try {
            new SettingsReader("lib/config.jbse").fillRunParameters(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final String[] splittedClasspath = classpath.split(":");
        final String[] finalClasspath = new String[splittedClasspath.length + 2];
        System.arraycopy(splittedClasspath, 0, finalClasspath, 2, splittedClasspath.length);
        finalClasspath[0] = "lib/jre/rt.jar";
        finalClasspath[1] = GET_PATH_OF_JAR_FROM_CLASS.apply(Run.class);
        p.addClasspath(finalClasspath);
        p.setMethodSignature(
                testMethod.getParent(CtClass.class).getQualifiedName().replaceAll("\\.", "/"),
                methodToDescriptor.apply(testMethod),
                testMethod.getSimpleName()
        );
        p.setDecisionProcedureType(RunParameters.DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath("lib/z3/build/bin/z3");
        p.setOutputFileName("runIf_z3.txt");
        p.setStepShowMode(RunParameters.StepShowMode.LEAVES);
        p.setDepthScope(JBSERunner.depthScope);
        p.setCountScope(JBSERunner.countScope);
        p.setStateFormatMode(RunParameters.StateFormatMode.FULLTEXT);
        p.setShowOnConsole(true);
        final Run r = new Run(p);
        r.run();
        return filterDistinctLeaves(r.getStates())
                .stream()
                .map(JBSERunner::buildConditionsOnArguments)
                .collect(Collectors.toList());
    }

    // TODO we can discard some cases, for instance, that trigger the same.
    // TODO implement the filter
    private static List<State> filterDistinctLeaves(List<State> states) {
        return states;
    }

    private static LinkedHashMap<String, String> getOriginOfOperand(Expression expression) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>(); // using LinkedHashMap to keep the insertion order
        getOriginForGivenOperand(expression.getFirstOperand(), map);
        getOriginForGivenOperand(expression.getSecondOperand(), map);
        return map;
    }

    private static void getOriginForGivenOperand(Primitive operand, final LinkedHashMap<String, String> map) {
        if (operand instanceof Expression) {
            map.putAll(getOriginOfOperand((Expression) operand));
        } else if (operand instanceof PrimitiveSymbolic) {
            String firstOperand = ((PrimitiveSymbolic) operand).getOrigin()
                    .toString().substring("{ROOT}:".length());
            if (firstOperand.startsWith("__PARAM") && firstOperand.endsWith("]")) {
                firstOperand = "param" + firstOperand.split("\\[")[1].substring(0, 1);
            }
            map.put(firstOperand, operand.toString());
        } else if (operand instanceof NarrowingConversion) {
            getOriginForGivenOperand(((NarrowingConversion) operand).getArg(), map);
        } else if (operand instanceof WideningConversion) {
            getOriginForGivenOperand(((WideningConversion) operand).getArg(), map);
        }
    }

    private static Map<String, List<String>> buildConditionsOnArguments(State state) {
        Map<String, List<String>> conditions = new HashMap<>(); // PARAM and conditions on it
        state.getPathCondition().forEach(clause -> {
            if (clause instanceof ClauseAssume) {
                final Primitive condition = ((ClauseAssume) clause).getCondition();
                final Map<String, String> origins = getOriginOfOperand((Expression) condition);
                String currentParam = new ArrayList<>(origins.keySet()).get(0);
                final String conditionWithOrigin = origins.keySet()
                        .stream()
                        .reduce(condition.toString(),
                                (conditionAsString, variable) ->
                                        conditionAsString.replace(origins.get(variable), variable)
                        );
                if (!conditions.containsKey(currentParam)) {
                    conditions.put(currentParam, new ArrayList<>());
                }
                conditions.get(currentParam).add(conditionWithOrigin);
            }
        });
        return conditions;
    }

    private static final Function<CtMethod<?>, String> methodToDescriptor = ctMethod ->
            "(" + ctMethod.getParameters().stream()
                    .map(CtTypedElement::getType)
                    .map(CtTypeReference::getSimpleName)
                    .filter(typeToDescriptor::containsKey)
                    .map(typeToDescriptor::get)
                    .collect(Collectors.joining("")) + ")V";

}
