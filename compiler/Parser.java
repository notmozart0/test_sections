import java.util.ArrayList;
import java.util.Scanner;

// ================= EXPRESSIONS =================

abstract class Expression {
}

// Number expression
class NumberExpression extends Expression {

    String value;

    NumberExpression(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}

// Variable expression
class VariableExpression extends Expression {

    String name;

    VariableExpression(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}

// Binary expression
class BinaryExpression extends Expression {

    Expression left;
    String operator;
    Expression right;

    BinaryExpression(Expression left, String operator, Expression right) {

        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public String toString() {

        return "(" + left + " " + operator + " " + right + ")";
    }
}

// ================= STATEMENTS =================

abstract class Statement {
}

// LET statement
class LetStatement extends Statement {

    String variableName;
    Expression value;

    LetStatement(String variableName, Expression value) {

        this.variableName = variableName;
        this.value = value;
    }

    public String toString() {

        return "LET " + variableName + " = " + value;
    }
}

// PRINT statement
class PrintStatement extends Statement {

    Expression expression;

    PrintStatement(Expression expression) {
        this.expression = expression;
    }

    public String toString() {

        return "PRINT " + expression;
    }
}

// ================= PARSER =================

public class Parser {

    private ArrayList<Lexer.Token> tokens;

    private int current = 0;

    // Constructor
    public Parser(ArrayList<Lexer.Token> tokens) {

        if (tokens == null || tokens.isEmpty()
                || !tokens.get(tokens.size() - 1).type.equals("EOF")) {

            throw new IllegalArgumentException(
                    "Parser needs a token list ending with EOF"
            );
        }

        this.tokens = tokens;
    }

    // Parse the complete program
    public ArrayList<Statement> parse() {

        ArrayList<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {

            Statement statement = parseStatement();

            consume("NL", "Expected ;) at the end of the statement");

            statements.add(statement);
        }

        return statements;
    }

    // Parse LET or PRINT
    private Statement parseStatement() {

        if (match("LET")) {

            Lexer.Token variable = consume(
                    "ID",
                    "Expected a variable name after LET"
            );

            consume(
                    "EQUALS",
                    "Expected = after the variable name"
            );

            Expression value = parseExpression();

            return new LetStatement(variable.lexeme, value);
        }

        if (match("PRINT")) {

            Expression expression = parseExpression();

            return new PrintStatement(expression);
        }

        throw syntaxError(
                peek(),
                "Expected LET or PRINT to start a statement"
        );
    }

    // Parse addition expressions
    private Expression parseExpression() {

        Expression expression = parseTerm();

        while (match("PLUS")) {

            String operator = previous().lexeme;

            Expression right = parseTerm();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    // Parse multiplication expressions
    private Expression parseTerm() {

        Expression expression = parsePrimary();

        while (match("STAR")) {

            String operator = previous().lexeme;

            Expression right = parsePrimary();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    // Parse numbers and variables
    private Expression parsePrimary() {

        if (peek().type.startsWith("NUM(")) {

            Lexer.Token number = advance();

            return new NumberExpression(number.lexeme);
        }

        if (match("ID")) {

            return new VariableExpression(previous().lexeme);
        }

        throw syntaxError(
                peek(),
                "Expected a number or variable in the expression"
        );
    }

    // Check whether current token matches
    private boolean match(String type) {

        if (check(type)) {

            advance();

            return true;
        }

        return false;
    }

    // Check current token without consuming it
    private boolean check(String type) {

        if (isAtEnd()) {
            return type.equals("EOF");
        }

        return peek().type.equals(type);
    }

    // Consume expected token
    private Lexer.Token consume(String type, String message) {

        if (check(type)) {
            return advance();
        }

        throw syntaxError(peek(), message);
    }

    // Move to next token
    private Lexer.Token advance() {

        Lexer.Token token = peek();

        if (!isAtEnd()) {
            current++;
        }

        return token;
    }

    // Get current token
    private Lexer.Token peek() {

        return tokens.get(current);
    }

    // Get previous token
    private Lexer.Token previous() {

        return tokens.get(current - 1);
    }

    // Check EOF
    private boolean isAtEnd() {

        return peek().type.equals("EOF");
    }

    // Generate syntax error
    private RuntimeException syntaxError(Lexer.Token token, String message) {

        return new RuntimeException(
                "Syntax error at line " + token.line +
                ", column " + token.column +
                ": " + message +
                ". Found '" + token.lexeme +
                "' (" + token.type + ")"
        );
    }

    // ================= MAIN =================

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        StringBuilder source = new StringBuilder();

        System.out.println("Enter your source code:");
        System.out.println("Type END on a separate line to finish.\n");

        // Read source code
        while (scanner.hasNextLine()) {

            String input = scanner.nextLine();

            if (input.equals("END")) {
                break;
            }

            source.append(input).append("\n");
        }

        try {

            // LEXER

            Lexer lexer = new Lexer(source.toString());

            ArrayList<Lexer.Token> tokens = lexer.tokenize();

            System.out.println("\nLEXER OUTPUT:\n");

            System.out.printf("%-6s %-6s %-8s %-15s %-15s %s%n",
                    "No.", "Line", "Column",
                    "Lexeme", "Token", "Description");

            for (Lexer.Token token : tokens) {

                System.out.printf("%-6d %-6d %-8d %-15s %-15s %s%n",
                        token.number,
                        token.line,
                        token.column,
                        token.lexeme,
                        token.type,
                        token.description);
            }

            // PARSER

            System.out.println("\nPARSER OUTPUT:\n");

            Parser parser = new Parser(tokens);

            ArrayList<Statement> statements = parser.parse();

            System.out.println("Parsing successful!\n");

            System.out.println("SYNTAX TREE:");

            for (Statement statement : statements) {

                System.out.println(statement);
            }

        }
        catch (RuntimeException error) {

            System.out.println("\nCompiler Error: " + error.getMessage());
        }

        scanner.close();
    }
}