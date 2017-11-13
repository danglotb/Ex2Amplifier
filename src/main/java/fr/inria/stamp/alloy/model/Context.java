package fr.inria.stamp.alloy.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/17
 */
public class Context {

    private static final String NAME_VECTOR_PARAMETER = "parameterVector";

    private static final String NAME_OF_FIELD_INT = "parameter_value_int_";

    private static final String NAME_OF_FIELD_STRING = "parameter_value_string_";

    private Map<String, String> parameterNameToParameterOfVector;

    private Queue<Variable> stackParameter;

    private int maxNumberParameterInt;

    public Context() {
        this.stackParameter = new ArrayDeque<>();
        this.maxNumberParameterInt = 0;
        this.parameterNameToParameterOfVector = new HashMap<>();
    }

    public void stack(Variable variable) {
        this.parameterNameToParameterOfVector.put(variable.name,
                NAME_VECTOR_PARAMETER + "." + NAME_OF_FIELD_INT + maxNumberParameterInt
        );
        this.maxNumberParameterInt++;
        this.stackParameter.add(variable);
    }

    public void matchParameterToVariable(Variable parameter) {
        final Variable variable = this.stackParameter.poll();
        this.parameterNameToParameterOfVector.put(parameter.name, this.parameterNameToParameterOfVector.get(variable.name));
    }

    public String parameterNameToVectorParameter(String parameter) {
        return this.parameterNameToParameterOfVector.get(parameter);
    }

    public String replaceByContext(String modification, String context) {
        return this.parameterNameToParameterOfVector.keySet()
                .stream()
                .filter(key ->
                        modification.contains(key.substring(context.length() + 1))
                                && key.startsWith(context)
                ).reduce(modification,
                        (accModification, parameter) ->
                                accModification.replaceAll(parameter.substring(context.length() + 1),
                                        this.parameterNameToVectorParameter(parameter)
                        )
                );
    }

    public void reset() {

    }

    public Context copy() {
        final Context copy = new Context();
        copy.maxNumberParameterInt = this.maxNumberParameterInt;
        copy.parameterNameToParameterOfVector = new HashMap<>(this.parameterNameToParameterOfVector);
        return copy;
    }

    public String toAlloy() {
        return "one sig parameterVector {\n" +
                IntStream.range(0, this.maxNumberParameterInt)
                        .mapToObj(operand -> "\t" + NAME_OF_FIELD_INT + operand + ":Int")
                        .collect(Collectors.joining(",\n"))
                /*+ IntStream.range(0, this.maxNumberParameterString)
                .mapToObj(operand -> "\t" + NAME_OF_FIELD_STRING + operand + ":String")
                .collect(Collectors.joining(",\n"))*/
                + "\n}\n";
    }
}
