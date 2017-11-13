package fr.inria.stamp.alloy.model;

import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.stamp.alloy.builder.ModelBuilder.nl;

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

    public Signature copy() {
        return new Signature(this.name,
                this.variables.stream()
                        .map(Variable::copy)
                        .collect(Collectors.toList())
        );
    }

    public String toString() {
        return "abstract sig " + this.name + " {" +
                nl +
                "\t" + this.variables.stream()
                        .map(Object::toString)
                        .map("\t"::concat)
                        .collect(Collectors.joining("," + nl)) +
                nl +
                "}";
    }


}
