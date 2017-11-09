package fr.inria.stamp.alloy.model;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String toAlloy() {
        return "\t" + constraint;
    }
}
