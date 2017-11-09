package fr.inria.stamp.alloy.model;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class Modification implements Fact {

    public String modifiedAttribute;

    public String modification;

    public Modification(String modifiedAttribute, String modification) {
        this.modifiedAttribute = modifiedAttribute;
        this.modification = modification;
    }

    @Override
    public String toAlloy() {
        return "\t" + modifiedAttribute + " = " +  modification;
    }
}
