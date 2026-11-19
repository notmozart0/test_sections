import java.util.ArrayList;
import java.util.HashSet;


public class SemanticAnalyzer {

   
    HashSet<String> declared = new HashSet<>();

   
    void checkExpr(Expression expr) {

        // A number is always valid.
        if (expr instanceof NumberExpression) {

            return;
        }

        // A variable must already be in the symbol table.
        if (expr instanceof VariableExpression) {

            String name = ((VariableExpression) expr).name;

            if (!declared.contains(name)) {

                throw new RuntimeException(
                        "Semantic error: variable '" + name + "' is not declared"
                );
            }
        }

        // A binary expression is valid only if both sides are valid.
        if (expr instanceof BinaryExpression) {

            BinaryExpression binary = (BinaryExpression) expr;

            checkExpr(binary.left);
            checkExpr(binary.right);
        }
    }

    // Check each statement in source order.
    void checkStatements(ArrayList<Statement> statements) {

        for (Statement statement : statements) {

            // LET: check the value, reject a duplicate name, then declare it.
            if (statement instanceof LetStatement) {

                LetStatement let = (LetStatement) statement;

                // Check the value first, so "LET x = x" fails.
                checkExpr(let.value);

                if (declared.contains(let.variableName)) {

                    throw new RuntimeException(
                            "Semantic error: variable '" +
                            let.variableName + "' is declared twice"
                    );
                }

                declared.add(let.variableName);
            }

            // PRINT: every variable in the printed expression must exist.
            else if (statement instanceof PrintStatement) {

                checkExpr(((PrintStatement) statement).expression);
            }
        }
    }

    // Start checking a whole program.
    void analyze(ArrayList<Statement> program) {

        declared.clear();

        checkStatements(program);
    }
}
