package fr.inria.stamp.alloy.runner;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class AlloyRunner {

    public static List<Object> run(String pathToModelAlloy) {
        try {
            List<Object> newValues = new ArrayList<>();
            CompModule model = CompUtil.parseEverything_fromFile(null, null, pathToModelAlloy);
            // Get the command to execute. for example :
            Command cmd = model.getAllCommands().get(0);
            // Execute the model using the command obtained in step 2
            A4Solution solution = TranslateAlloyToKodkod.execute_command(null,
                    model.getAllReachableSigs(),
                    cmd,
                    new A4Options()
            );
            Sig inputVectorSignature = getVectorOfInput(solution);
            for (Sig.Field field : inputVectorSignature.getFields()) {
                final TupleSet tupleSet = getTupleSet(solution, field);
                Universe universe = tupleSet.universe();
                newValues.add(universe.atom(tupleSet.iterator().next().index()));
            }
            return newValues;
        } catch (Err err) {
            throw new RuntimeException(err);
        }
    }

    private static Sig getVectorOfInput(A4Solution solution) {
        for (Sig signature : solution.getAllReachableSigs()) {
            if ("this/InputVector".equals(signature.label))
                return signature;
        }
        throw new RuntimeException("Could not find the definition of InputVector : Should not happen!");
    }

    private static TupleSet getTupleSet(A4Solution solution, Sig.Field fieldOfInputVector) {
        return solution.eval(fieldOfInputVector).debugGetKodkodTupleset();
    }

}
