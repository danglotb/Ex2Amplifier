package fr.inria.stamp.alloy.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class Model {

    private Queue<Variable> parameterStack;

    private List<Class<?>> registeredClass;

    private Map<Object, Integer> nbModificationPerReference;

    private List<Signature> signatures;

    private List<Fact> facts;

    private List<Variable> inputs;

    public Model() {
        this.signatures = new ArrayList<>();
        this.facts = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.nbModificationPerReference = new IdentityHashMap<>();
        this.registeredClass = new ArrayList<>();
        this.parameterStack = new ArrayDeque<>();
    }

    public List<Class<?>> getRegisteredClass() {
        return registeredClass;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public List<Variable> getInputs() {
        return inputs;
    }

    public Map<Object, Integer> getNbModificationPerReference() {
        return nbModificationPerReference;
    }

    public Queue<Variable> getParameterStack() {
        return parameterStack;
    }

    public int getIndexOfReferenceObject(Object object) {
        return new ArrayList<>(this.nbModificationPerReference.keySet()).indexOf(object);
    }

    public Signature getSignatureByName(String name) {
        return this.signatures.stream()
                .filter(signature -> name.equals(signature.name))
                .findFirst().orElseThrow(RuntimeException::new);
    }

}
