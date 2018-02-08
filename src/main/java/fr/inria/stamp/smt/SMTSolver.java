package fr.inria.stamp.smt;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/01/18
 */
public class SMTSolver {

    // TODO must be extended to other types
    private Map<String, NumeralFormula.IntegerFormula> variables;

    private SolverContext context;
    private FormulaManager fmgr;
    private BooleanFormulaManager bmgr;
    private IntegerFormulaManager imgr;

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
                throw new RuntimeException("Could not satisfy constraint" + constraint.toString());
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
                        .map(this::evaluate)
                        .reduce(this.bmgr.makeTrue(),
                                (acc, booleanFormula) -> this.bmgr.and(acc, booleanFormula)
                        )
                ).reduce(this.bmgr.makeTrue(),
                        (acc, booleanFormula) -> this.bmgr.and(acc, booleanFormula)
                );
    }

    private BooleanFormula evaluate(String constraint) {
        if (constraint.contains("==")) {
            final String[] operands = constraint.split("==");
            return this.imgr.equal(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("!=")) {
            final String[] operands = constraint.split("!=");
            return this.bmgr.not(this.imgr.equal(evaluateNum(operands[0]), evaluateNum(operands[1])));
        } else if (constraint.contains(">=")) {
            final String[] operands = constraint.split(">=");
            return this.imgr.greaterThan(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains(">")) {
            final String[] operands = constraint.split(">");
            return this.imgr.greaterOrEquals(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("<=")) {
            final String[] operands = constraint.split("<=");
            return this.imgr.lessOrEquals(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("<")) {
            final String[] operands = constraint.split("<");
            return this.imgr.lessOrEquals(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else {
            return null;
        }
    }

    private NumeralFormula.IntegerFormula evaluateNum(String constraint) {
        if (constraint.contains("+")) {
            final String[] operands = constraint.split("\\+");
            return this.imgr.add(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("-")) {
            final String[] operands = constraint.split("-");
            return this.imgr.subtract(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("*")) {
            final String[] operands = constraint.split("\\*");
            return this.imgr.multiply(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("/")) {
            final String[] operands = constraint.split("/");
            return this.imgr.divide(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else if (constraint.contains("%")) {
            final String[] operands = constraint.split("%");
            return this.imgr.modulo(evaluateNum(operands[0]), evaluateNum(operands[1]));
        } else {
            if (this.variables.containsKey(constraint)) {
                return this.variables.get(constraint);
            } else {
                return this.imgr.makeNumber(Integer.parseInt(constraint));
            }
        }
    }

    public static List<?> solve(Map<String, List<String>> constraintsPerParamName) {
        SMTSolver solver = new SMTSolver();
        BooleanFormula constraint = solver.buildConstraint(constraintsPerParamName);
        final Model model = solver.solve(constraint);
        return solver.getValues(model);
    }

}
