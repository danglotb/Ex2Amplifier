package fr.inria.stamp.alloy.model;

public class Variable {

    public final String type;

    public final String name;

    public Variable(String name, String type) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name + ":" + this.type;
    }
}