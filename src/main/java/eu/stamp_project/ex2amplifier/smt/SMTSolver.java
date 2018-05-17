package eu.stamp_project.ex2amplifier.smt;

import org.slf4j.LoggerFactory;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.NumeralFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/01/18
 */
public class SMTSolver {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SMTSolver.class);

    // TODO must be extended to other types
    private Map<String, NumeralFormula.IntegerFormula> variables;

    private SolverContext context;
    private FormulaManager fmgr;
    private BooleanFormulaManager bmgr;
    private IntegerFormulaManager imgr;
    private ScriptEngine engine;

    private SMTSolver() {
        try {
            Configuration config = Configuration.defaultConfiguration();
            LogManager logger = BasicLogManager.create(config);
            ShutdownManager shutdown = ShutdownManager.create();
            this.context = SolverContextFactory.createSolverContext(
                    config, logger, shutdown.getNotifier(), SolverContextFactory.Solvers.SMTINTERPOL);
            this.fmgr = this.context.getFormulaManager();
            this.bmgr = this.fmgr.getBooleanFormulaManager();
            this.imgr = this.fmgr.getIntegerFormulaManager();
            this.variables = new HashMap<>();
            this.engine = new ScriptEngineManager().getEngineByName("JavaScript");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Model solve(BooleanFormula constraint) {
        try (ProverEnvironment prover = context.newProverEnvironment(SolverContext.ProverOptions.GENERATE_MODELS)) {
            prover.addConstraint(constraint);
            boolean isUnsat = prover.isUnsat();
            if (!isUnsat) {
                return prover.getModel();
            } else {
                LOGGER.warn("Could not satisfy constraint{}", constraint.toString());
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<?> getValues(Model model) {
        return this.variables.keySet().stream()
                .map(this.variables::get)
                .map(model::evaluate)
                .collect(Collectors.toList());
    }

    private BooleanFormula buildConstraint(Map<String, List<String>> constraintsPerParamName) {
        constraintsPerParamName.keySet()
                .forEach(paramName ->
                        this.variables.put(paramName, this.imgr.makeVariable(paramName))
                );

        return constraintsPerParamName.keySet().stream()
                .map(constraintsPerParamName::get)
                .map(constraints -> constraints.stream()
                        .map(constraint -> constraint.replaceAll(" ", ""))
                        .flatMap(constraint -> Arrays.stream(constraint.split("&&")))
                        .map(this::evaluate)
                        .filter(Objects::nonNull)
                        .reduce(this.bmgr.makeTrue(),
                                (acc, booleanFormula) -> this.bmgr.and(acc, booleanFormula)
                        )
                ).reduce(this.bmgr.makeTrue(),
                        (acc, booleanFormula) -> this.bmgr.and(acc, booleanFormula)
                );
    }

    private BooleanFormula evaluate(String constraint) {
        try {
            if (constraint.contains("==")) {
                final String[] operands = constraint.split("==");
                return this.imgr.equal(eval(operands[0]), eval(operands[1]));
            } else if (constraint.contains("!=")) {
                final String[] operands = constraint.split("!=");
                return this.bmgr.not(this.imgr.equal(eval(operands[0]), eval(operands[1])));
            } else if (constraint.contains(">=")) {
                final String[] operands = constraint.split(">=");
                return this.imgr.greaterThan(eval(operands[0]), eval(operands[1]));
            } else if (constraint.contains(">")) {
                final String[] operands = constraint.split(">");
                return this.imgr.greaterOrEquals(eval(operands[0]), eval(operands[1]));
            } else if (constraint.contains("<=")) {
                final String[] operands = constraint.split("<=");
                return this.imgr.lessOrEquals(eval(operands[0]), eval(operands[1]));
            } else if (constraint.contains("<")) {
                final String[] operands = constraint.split("<");
                return this.imgr.lessOrEquals(eval(operands[0]), eval(operands[1]));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private NumeralFormula.IntegerFormula evaluateOperands(String constraint, String operator) {
        // the given operator is the first one in th String constraint
        String escapedOperator = String.format("\\%s", operator);
        final String[] operands = constraint.split(escapedOperator);
        // we concat all the operands from operand[1] to operands[operands.length]
        StringBuilder operandRight = new StringBuilder();
        for (int i = 1; i < operands.length; i++) {
            operandRight.append(operands[i]);
        }
        final NumeralFormula.IntegerFormula eval = eval(operands[0]);
        if (eval == null) {
            return null;
        }
        final NumeralFormula.IntegerFormula eval1 = eval(operandRight.toString());
        if (eval1 == null) {
            return null;
        }
        switch (operator) {
            case "+":
                return this.imgr.add(eval, eval1);
            case "-":
                return this.imgr.subtract(eval, eval1);
            case "/":
                return this.imgr.divide(eval, eval1);
            case "*":
                return this.imgr.multiply(eval, eval1);
            case "%":
                return this.imgr.modulo(eval, eval1);
            default:
                return null;
        }
    }

    private List<String> supportedOperators = Arrays.asList("+", "-", "/", "*", "%");

    private String findFirstOperator(String constraint) {
        int nbParenthesis = 0;
        while (nbParenthesis != -constraint.length() / 2) {
            for (int i = 0; i < constraint.length(); i++) {
                if (nbParenthesis == 0 && supportedOperators.contains("" + constraint.charAt(i))) {
                    return "" + constraint.charAt(i);
                }
                nbParenthesis += constraint.charAt(i) == '(' ? 1 : 0;
                nbParenthesis += constraint.charAt(i) == ')' ? -1 : 0;
            }
            nbParenthesis--; // we go deeper, i.e. inside parenthesis
        }
        return "";
    }

    private int findIndexOfMatchingParenthesis(String constraint) {
        int nbParenthesis = 0;
        for (int i = 0; i < constraint.length(); i++) {
            if (constraint.charAt(i) == '(') {
                nbParenthesis++;
            } else if (constraint.charAt(i) == ')') {
                if (nbParenthesis == 1) {
                    return i;
                } else {
                    nbParenthesis--;
                }
            }
        }
        throw new RuntimeException(String.format("Not well formatted constraint: %s", constraint));
    }

    private String removeConversionCall(String constraint, String conversion) {
        // conversion.length() + 3 because the format is: CONVERSION-T(ExpressionToBeConverted), where T is the target type
        // we want to get ExpressionToBeConverted
        return removeParenthesisIfNeeded(constraint.substring(conversion.length() + 3, findIndexOfMatchingParenthesis(constraint)));
    }

    private String removeParenthesisIfNeeded(String constraint) {
        if (constraint.startsWith("NARROW")) {
            return removeConversionCall(constraint, "NARROW");
        } else if (constraint.startsWith("WIDEN")) {
            return removeConversionCall(constraint, "WIDEN");
        } else if (!(constraint.startsWith("(") && constraint.endsWith(")"))) {
            return constraint;
        } else {
            int nbParenthesis = 1;
            // we must checks that the last parenthesis match with the first one
            for (int i = 1; i < constraint.length(); i++) {
                if (constraint.charAt(i) == '(') {
                    nbParenthesis++;
                } else if (constraint.charAt(i) == ')') {
                    nbParenthesis--;
                    if (nbParenthesis == 0 && i + 1 < constraint.length()) {
                        return constraint;
                    }
                }
            }
        }
        return constraint.substring(1, constraint.length() - 1);
    }

    private NumeralFormula.IntegerFormula eval(String constraint) {
        constraint = removeParenthesisIfNeeded(constraint);
        if (constraint.startsWith("-")) {
            return this.imgr.subtract(this.imgr.makeNumber(0), eval(constraint.substring(1)));
        } else if (constraint.startsWith("~")) {
            return this.imgr.negate(eval(constraint.substring(1)));
        }
        final String operator = findFirstOperator(constraint);
        if (operator.isEmpty()) {
            // there is no operator, i.e. it is an operand
            if (this.variables.containsKey(constraint)) {
                return this.variables.get(constraint);
            } else if (constraint.contains(">") || constraint.contains("<")) {// the SMT solver does not support such operation
                try {
                    Object value = this.engine.eval(constraint);
                    if (value instanceof Double) {
                        return this.imgr.makeNumber((Double) value);
                    } else {
                        return this.imgr.makeNumber((Integer) value);
                    }
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    return this.imgr.makeNumber(Long.parseLong(constraint));
                } catch (Exception e) {
                    LOGGER.warn("Error while parsing {}", constraint);
                    LOGGER.warn("should support it"); // TODO must be implemented!
                    return null;
                }
            }
        } else {
            return evaluateOperands(constraint, operator);
        }
    }

    public static List<?> solve(Map<String, List<String>> constraintsPerParamName) {
        SMTSolver solver = new SMTSolver();
        BooleanFormula constraint = solver.buildConstraint(constraintsPerParamName);
        final Model model = solver.solve(constraint);
        if (model == null) {
            return Collections.emptyList();
        } else {
            return solver.getValues(model);
        }
    }

}
