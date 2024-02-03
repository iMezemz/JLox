package lox;


public class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;

            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (right.equals(0.0)) {
                    throw new RuntimeError(expr.operator, "Division by zero error");
                }
                return (double) left / (double) right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) { // Normal number addition
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) { // String concatenation
                    return (String) left + (String) right;
                }

                if (left instanceof String && right instanceof Double) { // String Number concatenation
                    return (String) left + right.toString();
                }

                if (left instanceof Double && right instanceof String) { // String Number concatenation
                    return left.toString() + (String) right;
                }

                throw new RuntimeError(expr.operator, "Only numbers and strings can be used as operands");

            case BANG_EQUAL:
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);

            case COMMA:
                return right;

        }

        return null;
    }


    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperands(expr.operator, right);
                return -(double) right;
        }

        return null;
    }


    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (isTruthy(condition)) {
            return left;
        }
        return right;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false; // null is false
        if (object instanceof Boolean) return (Boolean) object; // evaluate boolean value
        return true; // anything else is true
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperands(Token operator, Object... operands) {
        for (Object operand : operands) {
            if (!(operand instanceof Double))
                throw new RuntimeError(operator, "Operand" + (operands.length > 1 ? "s" : "") + " must be a number.");
        }
    }


}
