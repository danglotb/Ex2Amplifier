package fr.inria.stamp.alloy.builder;

import fr.inria.stamp.alloy.model.Constraint;
import fr.inria.stamp.alloy.model.Fact;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.alloy.model.Modification;
import fr.inria.stamp.alloy.model.Signature;
import fr.inria.stamp.alloy.model.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class ModelBuilder {

    private static List<List<Fact>> dependantFacts = new ArrayList<>();

    public static Model model = new Model();

    public static final String nl = System.getProperty("line.separator");

    private static void addSignature(String name, Variable... variables) {
        model.getSignatures().add(new Signature(name, Arrays.asList(variables)));
    }

    public static void depopParameters(Variable... parameters) {
        Arrays.stream(parameters).forEach(parameter -> {
                    final Variable variable = model.getParameterStack().poll();
                    model.getContext().updateParameter(variable.name, parameter.name, variable.type);
                }
        );
    }

    public static void addInputs(Variable... types) {
        Arrays.stream(types)
                .map(variable ->
                        new Variable(variable.name.split("\\.")[1], variable.type)
                ).forEach(model.getInputs()::add);
    }

    public static void addParameters(Variable... parameters) {
        model.getContext().reset();
        Arrays.stream(parameters)
                .map(parameter -> {
                    model.getParameterStack().add(parameter);
                    model.getContext().addParameter(parameter);
                    return new Modification(
                            model.getContext().parameterNameToVectorParameter(parameter.name),
                            parameter.name
                    );
                }).forEach(model.getFacts()::add);
    }

    public static void addModification(Object object, String modifiedAttribute, String modification, Variable... variables) {
        final Class<?> objectClass = object.getClass();
        // each class has one abstract sig
        if (!model.getRegisteredClass().contains(objectClass)) {
            addSignature(objectClass.getName().replaceAll("\\.", "_"), variables);
            model.getRegisteredClass().add(objectClass);
        }
        // each instance (object) has a list of modification
        if (!model.getNbModificationPerReference().containsKey(object)) {
            model.getNbModificationPerReference().put(object, 0);
        }

        //rename the modification and variables according to the context.
        final String prefixObject = objectClass.getName().replaceAll("\\.", "_") + "_" + model.getIndexOfReferenceObject(object) + "_";
        final Integer count = model.getNbModificationPerReference().get(object);
        modification = modification.replaceAll(modifiedAttribute,
                prefixObject + count + "." + modifiedAttribute
        );
        final Modification modificationObject = new Modification(
                prefixObject + (count + 1) + "." + modifiedAttribute,
                model.getContext().replaceByContext(modification)
        );
        dependantFacts.forEach(facts -> facts.add(modificationObject));
        model.getFacts().add(modificationObject);
        model.getNbModificationPerReference().put(object, count + 1);
    }

    public static void addConstraint(Object object, final String constraint) {
        final Class<?> objectClass = object.getClass();
        final String prefixObject = objectClass.getName().replaceAll("\\.", "_") + "_" + model.getIndexOfReferenceObject(object) + "_";
        final Integer count = model.getNbModificationPerReference().get(object);
        final Signature signatureByName = model.getSignatureByName(object.getClass().getName().replaceAll("\\.", "_"));
        final Constraint constraintObject = new Constraint(
                signatureByName.variables.stream()
                        .filter(variable -> constraint.contains(variable.name))
                        .map(variable -> variable.name)
                        .reduce(constraint,
                                (accConstraint, variable) ->
                                        accConstraint.replaceAll(variable, prefixObject + count + "." + variable)
                        )
        );
        dependantFacts.add(new ArrayList<>());
        dependantFacts.forEach(facts -> facts.add(constraintObject));
        model.getFacts().add(constraintObject);
    }

    public static void endConstraint() {
        if (!(dependantFacts.get(dependantFacts.size() - 1).get(0) instanceof Constraint)) {
            throw new RuntimeException("The first fact of the dependant facts must be a constraint");
        }
        final Constraint constraint = (Constraint) (dependantFacts.get(dependantFacts.size() - 1).remove(0));
        constraint.subFacts.addAll(dependantFacts.remove(dependantFacts.size() - 1));
    }

    public static void addInput(Variable variable) {
        model.getInputs().add(variable);
    }

    public static String toAlloy() {
        StringBuilder modelAlloy = new StringBuilder();
        modelAlloy.append("one sig InputVector {")
                .append(nl)
                .append(model.getInputs()
                .stream()
                .map(Object::toString)
                .map("\t"::concat)
                .collect(Collectors.joining("," + nl)))
                .append(nl)
                .append("}");
        modelAlloy.append(model.getContext().toAlloy());
        model.getSignatures().forEach(modelAlloy::append);

        final ArrayList<Object> keys = new ArrayList<>(model.getNbModificationPerReference().keySet());
        keys.forEach(key ->
                IntStream.range(1, model.getNbModificationPerReference().get(key) + 1)
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
                .append(model.getFacts()
                        .stream()
                        .map(Fact::toAlloy)
                        .collect(Collectors.joining(nl)))
                .append(nl)
                .append("}")
                .append(nl);
        modelAlloy.append("run {} for ").append(model.getInputs().size());
        return modelAlloy.toString();
    }

    public static Model getModel() {
        return model;
    }
}
