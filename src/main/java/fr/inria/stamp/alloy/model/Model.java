package fr.inria.stamp.alloy.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.inria.stamp.alloy.builder.ModelBuilder.nl;

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

    private Context context;

    private int indexOfConstraintToBeNegated;

    public Model() {
        this.signatures = new ArrayList<>();
        this.facts = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.nbModificationPerReference = new IdentityHashMap<>();
        this.registeredClass = new ArrayList<>();
        this.parameterStack = new ArrayDeque<>();
        this.indexOfConstraintToBeNegated = 0;
        this.context = new Context();
    }

    private Model(Model model) {
        this.signatures = model.signatures.stream().map(Signature::copy).collect(Collectors.toList());
        this.facts = model.facts.stream().map(Fact::copy).collect(Collectors.toList());
        copySubFacts(model);
        this.inputs = model.inputs.stream().map(Variable::copy).collect(Collectors.toList());
        this.nbModificationPerReference = new IdentityHashMap<>(model.getNbModificationPerReference());
        this.registeredClass = new ArrayList<>(model.getRegisteredClass());
        this.parameterStack = new ArrayDeque<>();
        this.indexOfConstraintToBeNegated = 0;
        this.context = model.context.copy();
    }

    private void copySubFacts(Model model) {
        this.facts.stream()
                .filter(fact -> fact instanceof Constraint)
                .map(fact -> (Constraint) fact)
                .forEach(fact -> {
                    Constraint originalConstraint = (Constraint) model.facts.stream()
                            .filter(fact1 -> fact1.equals(fact))
                            .findFirst()
                            .orElseThrow(RuntimeException::new);
                    originalConstraint.subFacts.forEach(subFact -> fact.subFacts.add(
                            this.facts.stream()
                                    .filter(fact1 -> fact1.equals(subFact))
                                    .findFirst()
                                    .orElseThrow(RuntimeException::new)
                            )
                    );
                });
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

    public Context getContext() {
        return context;
    }

    public Model negateNextConstraint() {
        final Model model = new Model(this);
        final Constraint negatedConstraint = model.getFacts()
                .stream()
                .filter(fact -> fact instanceof Constraint)
                .map(fact -> (Constraint) fact)
                .collect(Collectors.toList())
                .get(this.indexOfConstraintToBeNegated++);
        negatedConstraint.constraint = negatedConstraint.constraint.startsWith("not") ?
                negatedConstraint.constraint.substring(4) :
                "not " + negatedConstraint.constraint;
        model.facts = model.getFacts().stream()
                .filter(fact -> !(negatedConstraint.subFacts.contains(fact)))
                .collect(Collectors.toList());
        return model;
    }

    public String toAlloy() {
        StringBuilder modelAlloy = new StringBuilder();
        modelAlloy.append("one sig InputVector {")
                .append(nl)
                .append(this.getInputs()
                        .stream()
                        .map(Object::toString)
                        .map("\t"::concat)
                        .collect(Collectors.joining("," + nl)))
                .append(nl)
                .append("}")
                .append(nl);
        modelAlloy.append(this.getContext().toAlloy());
        modelAlloy.append(this.getSignatures()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(nl))
        ).append(nl);

        final ArrayList<Object> keys = new ArrayList<>(this.getNbModificationPerReference().keySet());
        keys.forEach(key ->
                IntStream.range(1, this.getNbModificationPerReference().get(key) + 1)
                        .mapToObj(value -> value + "")
                        .forEach(index ->
                                modelAlloy.append("one sig ")
                                        .append(key.getClass().getName().replaceAll("\\.", "_"))
                                        .append("_")
                                        .append(keys.indexOf(key))
                                        .append("_")
                                        .append(index).append(" extends ")
                                        .append(key.getClass().getName().replaceAll("\\.", "_"))
                                        .append("{}")
                                        .append(nl)
                        )
        );
        modelAlloy.append("fact {")
                .append(nl)
                .append(this.getFacts()
                        .stream()
                        .map(Fact::toAlloy)
                        .collect(Collectors.joining(nl)))
                .append(nl)
                .append("}")
                .append(nl);
        modelAlloy.append("run {} for ").append(this.getInputs().size());
        return modelAlloy.toString();
    }

}
