package fr.inria.stamp.alloy.builder;

import fr.inria.stamp.alloy.model.Variable;

import java.util.ArrayList;
import java.util.List;
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

    private List<String> parametersInt;

    private List<String> parametersString;

    private int maxNumberParameterInt;

    private int maxNumberParameterString;

    public Context() {
        this.parametersInt = new ArrayList<>();
        this.parametersString = new ArrayList<>();
        this.maxNumberParameterInt = 0;
        this.maxNumberParameterString = 0;
    }

    public void addParameter(Variable parameter) {
        if ("Int".equals(parameter.type)) {
            this.parametersInt.add(parameter.name);
            if (this.parametersInt.size() > this.maxNumberParameterInt) {
                this.maxNumberParameterInt++;
            }
        } else {
            this.parametersString.add(parameter.name);
            if (this.parametersString.size() > this.maxNumberParameterString) {
                this.maxNumberParameterString++;
            }
        }
    }

    public String parameterNameToVectorParameter(String parameter) {
        return NAME_VECTOR_PARAMETER + "." +
                (this.parametersInt.contains(parameter) ?
                        NAME_OF_FIELD_INT + this.parametersInt.indexOf(parameter) :
                        NAME_OF_FIELD_STRING + this.parametersString.indexOf(parameter));
    }

    private List<String> containAParameter(String modification) {
        if (this.parametersInt.stream().anyMatch(modification::contains)) {
            return this.parametersInt;
        } else if (this.parametersString.stream().anyMatch(modification::contains)) {
            return this.parametersString;
        } else {
            return null;
        }
    }

    public String replaceByContext(String modification) {
        final List<String> concernedList = containAParameter(modification);
        if (concernedList == null) {
            return modification;
        }
        return concernedList.stream()
                .filter(modification::contains)
                .reduce(modification,
                        (accModification, parameter) -> accModification.replaceAll(parameter,
                                this.parameterNameToVectorParameter(parameter)
                        )
                );
    }

    public void reset() {
        this.parametersString.clear();
        this.parametersInt.clear();
    }

    public void updateParameter(String oldName, String newName, String type) {
        if ("Int".equals(type)) {
            this.parametersInt.remove(oldName);
            this.parametersInt.add(newName);
        } else {
            this.parametersString.remove(oldName);
            this.parametersInt.add(newName);
        }
    }

    public String toAlloy() {
        return "one sig parameterVector {\n" +
                IntStream.range(0, this.maxNumberParameterInt)
                        .mapToObj(operand -> "\t" + NAME_OF_FIELD_INT + operand + ":Int")
                        .collect(Collectors.joining("\n"))
                + IntStream.range(0, this.maxNumberParameterString)
                .mapToObj(operand -> "\t" + NAME_OF_FIELD_STRING + operand + ":String")
                .collect(Collectors.joining("\n"))
                + "\n}\n";
    }
}
