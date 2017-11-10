package fr.inria.stamp.alloy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class Constraint implements Fact {

    public List<Fact> subFacts;

    public String constraint;

    public Constraint(String constraint) {
        this.constraint = constraint;
        this.subFacts = new ArrayList<>();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof Constraint)) {
            return false;
        }
        Constraint constraint = (Constraint) o;
        return constraint.constraint.equals(this.constraint);
    }

    @Override
    public String toAlloy() {
        return "\t" + constraint;
    }

    @Override
    public Fact copy() {
        final Constraint copy = new Constraint(this.constraint);
        copy.subFacts = this.subFacts.stream()
                .map(Fact::copy)
                .collect(Collectors.toList());
        return copy;
    }
}
