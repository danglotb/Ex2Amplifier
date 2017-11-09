package fr.inria.stamp.alloy.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class Signature {

    public final String name;

    public final List<Variable> variables;

    public Signature(String name, List<Variable> variables) {
        this.name = name;
        this.variables = variables;
    }

    public String toString() {
        return new StringBuilder().append("abstract sig ")
                .append(this.name)
                .append(" {\n\t")
                .append(this.variables.stream()
                            .map(Object::toString)
                            .map("\t"::concat)
                            .collect(Collectors.joining(",\n"))
                ).append("\n}")
                .toString();
    }


}
