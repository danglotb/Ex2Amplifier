package fr.inria.stamp.alloy.model;

public class Variable {

    public final String type;

    public final String name;

    public Variable(String name, String type) {
        this.type = type;
        this.name = name;
    }

    public Variable copy() {
        return new Variable(this.name, this.type);
    }

    @Override
    public String toString() {
        return this.name + ":" + this.type;
    }
}