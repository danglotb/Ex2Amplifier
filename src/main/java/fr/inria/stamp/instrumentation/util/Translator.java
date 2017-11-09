package fr.inria.stamp.instrumentation.util;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/17
 */
public class Translator {

    public static String toAlloy(CtExpression expression) {
        if (expression instanceof CtBinaryOperator) {
            return toAlloy((CtBinaryOperator) expression);
        } else if (expression instanceof CtLiteral) {
            return toAlloy((CtLiteral) expression);
        } else if (expression instanceof CtUnaryOperator) {
            return toAlloy((CtUnaryOperator) expression);
        } else if (expression instanceof CtOperatorAssignment) {
            return toAlloy((CtOperatorAssignment) expression);
        } else if (expression instanceof CtFieldAccess) {
            return toAlloy((CtFieldAccess)expression);
        } else if (expression instanceof CtAssignment) {
            return toAlloy((CtAssignment) expression);
        } else {
            return expression.toString();
        }
//        throw new RuntimeException("Not supported translation for " + expression.toString() + "("+ expression.getClass()+")");
    }

    private static String toAlloy(CtAssignment assignment) {
        return toAlloy(assignment.getAssignment());
    }

    private static String toAlloy(CtFieldAccess ctFieldAccess){
            return ctFieldAccess.getVariable().toString();
    }

    private static String toAlloy(CtOperatorAssignment assignment) {
        return toAlloy(assignment.getFactory()
                .createBinaryOperator(
                        assignment.getAssigned(),
                        assignment.getAssignment(),
                        assignment.getKind())
        );
    }

    private static String toAlloy(CtLiteral literal) {
        return literal.toString();
    }

    private static String toAlloy(CtBinaryOperator condition) {
        final BinaryOperatorKind binOp = condition.getKind();
        switch (binOp) {
            case OR:
                return toAlloy(condition.getLeftHandOperand()) + "||" + toAlloy(condition.getRightHandOperand());
            case AND:
                return toAlloy(condition.getLeftHandOperand()) + "&&" + toAlloy(condition.getRightHandOperand());
            case BITOR:
                return "|";
            case BITXOR:
                return "^";
            case BITAND:
                return "&";
            case EQ:
                return toAlloy(condition.getLeftHandOperand()) + "=" + toAlloy(condition.getRightHandOperand());
            case NE:
                return "not " + toAlloy(condition.getLeftHandOperand()) + "=" + toAlloy(condition.getRightHandOperand());
            case LT:
                return "<";
            case GT:
                return ">";
            case LE:
                return "<=";
            case GE:
                return ">=";
            case SL:
                return "<<";
            case SR:
                return ">>";
            case USR:
                return ">>>";
            case PLUS:
                return "plus[" + toAlloy(condition.getLeftHandOperand()) + "," + toAlloy(condition.getRightHandOperand()) + "]";
            case MINUS:
                return "sub[" + toAlloy(condition.getLeftHandOperand()) + "," + toAlloy(condition.getRightHandOperand()) + "]";
            case MUL:
                return "mul[" + toAlloy(condition.getLeftHandOperand()) + "," + toAlloy(condition.getRightHandOperand()) + "]";
            case DIV:
                return "div[" + toAlloy(condition.getLeftHandOperand()) + "," + toAlloy(condition.getRightHandOperand()) + "]";
            case MOD:
                return "rem[" + toAlloy(condition.getLeftHandOperand()) + "," + toAlloy(condition.getRightHandOperand()) + "]";
            case INSTANCEOF:
                return "instanceof";
            default:
                throw new RuntimeException("Unsupported operator " + binOp.name());
        }
    }

    private static String toAlloy(CtUnaryOperator unaryOperator) {
        final UnaryOperatorKind unaryOperatorKind = unaryOperator.getKind();
        switch (unaryOperatorKind) {
            case POS:
                return "+";
            case NEG:
                return "-";
            case NOT:
                return "not " + toAlloy(unaryOperator.getOperand());
            case COMPL:
                return "~";
            case PREINC:
                return "++";
            case PREDEC:
                return "--";
            case POSTINC:
                return "++";
            case POSTDEC:
                return "--";
            default:
                throw new RuntimeException("Unsupported operator " + unaryOperatorKind.name());
        }
    }

}
