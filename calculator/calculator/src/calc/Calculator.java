package calc;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    public double evalPostfix(List<String> postfix) {
        Stack<String> stack = new Stack<>();

        for (String token : postfix) {
            if (OPERATORS.contains(token)) {
                double d1 = Double.parseDouble(stack.pop());
                double d2 = Double.parseDouble(stack.pop());
                double res = applyOperator(token, d1, d2);
                stack.push(String.valueOf(res));
            } else { // Is number.
                stack.push(token);
            }
        }

        // Convert last remaining token on stack to double.
        return Double.parseDouble(stack.pop());
    }


    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    public List<String> infix2Postfix(List<String> tokens) {
        Stack<String> stack = new Stack<>();
        List<String> postfix = new ArrayList<>();

        for (String token : tokens) {
            if (OPERATORS.contains(token) || "()".contains(token)) {
                addToStack(token, stack, postfix);
            } else { // Is number.
                postfix.add(token);
            }
        }
        // Transfer remaining tokens on stack to postfix.
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    private void addToStack(String token, Stack<String> stack, List<String> postfix) {
        if (shouldBeginPopStack(token, stack)) {

            popStack(token, stack, postfix);

        } else if (token.equals(")")) {

            closeParenthesis(stack, postfix);
            // Don't push right parenthesis to stack.
            return;
        }

        // Add new token to stack.
        stack.push(token);
    }

    private boolean shouldBeginPopStack(String token, Stack<String> stack) {
        boolean stackNotEmpty = !stack.isEmpty();
        boolean tokenIsOperator = OPERATORS.contains(token);
        boolean stackIsOperator = stackNotEmpty && OPERATORS.contains(stack.peek());

        return stackNotEmpty && tokenIsOperator && stackIsOperator;
    }

    private void popStack(String token, Stack<String> stack, List<String> postfix) {
        // While there is something to pop.
        while (shouldPop(stack, token)) {
            // Remove from stack and add to postfix.
            String stackOp = stack.pop();
            postfix.add(stackOp);
        }
    }

    private boolean shouldPop(Stack<String> stack, String token) {
        boolean notEmpty = !stack.isEmpty();
        boolean leftAssociative = getAssociativity(token) == Assoc.LEFT;
        boolean higherOrEqualPrecedence = notEmpty && getPrecedence(stack.peek()) >= getPrecedence(token);

        return notEmpty && leftAssociative && higherOrEqualPrecedence;
    }

    private void closeParenthesis(Stack<String> stack, List<String> postfix) {
        // Loop until opening parenthesis or end of stack.
        while (!stack.isEmpty() && !stack.peek().equals("(")) {
            String popped = stack.pop();
            // Don't add parenthesis to postfix.
            if (!"()".contains(popped)) {
                postfix.add(popped);
            }
        }
        // Remove left parenthesis.
        stack.pop();
    }


    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    public List<String> tokenize(String expr) {
        // Filtrate anything that is not a digit or an operator or a parenthesis or a comma or a period.
        expr = expr.replaceAll("[^-+*/^() ,.[0-9]]*", "");

        // Split at every operand and parenthesis.
        List<String> tokens = new ArrayList<>(Arrays.asList(expr.split("(?<=[-+*/()^ ])|(?=[-+*/()^ ])")));

        // Remove tokens containing only a space.
        tokens.removeIf(" "::equals);
        for(int i = 0; i < tokens.size(); i++) {
            // Replace all periods and commas with a single period.
            tokens.set(i, tokens.get(i).replaceAll("(\\.+)|(,+)", "."));
        }

        // Check for errors in input.
        validateInput(tokens);

        return tokens;
    }

    private void validateInput(List<String> filteredTokens) {
        // Count stuff.
        int operatorCount = 0, operandCount = 0, openParenCount = 0, closeParenCount = 0;
        for (String token : filteredTokens) {
            if (OPERATORS.contains(token)) {
                operatorCount++;
            } else if (token.equals("(")) {
                openParenCount++;
            } else if (token.equals(")")) {
                closeParenCount++;
            } else {
                operandCount++;
            }
        }
        if (operatorCount >= operandCount) {
            throw new IllegalArgumentException(MISSING_OPERAND);
        }
        if (operandCount > operatorCount + 1 ||
                openParenCount != closeParenCount) {
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
    }
}
