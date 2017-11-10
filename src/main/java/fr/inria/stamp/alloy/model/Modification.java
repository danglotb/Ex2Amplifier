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

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof Modification)) {
            return false;
        }
        Modification modification = (Modification) o;
        return modification.modification.equals(this.modification) &&
                modification.modifiedAttribute.equals(this.modifiedAttribute);
    }

    @Override
    public String toAlloy() {
        return "\t" + modifiedAttribute + " = " +  modification;
    }

    @Override
    public Fact copy() {
        return new Modification(this.modifiedAttribute, this.modification);
    }
}
