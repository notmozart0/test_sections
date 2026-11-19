import java.util.ArrayList;

// Turns the checked syntax tree into a Java program.
public class CodeGenerator {

    String indent(int depth) {

        String spaces = "";

        for (int i = 0; i < depth; i++) {
            spaces += "    ";
        }

        return spaces;
    }

    String expression(Expression e) {

        if (e instanceof NumberExpression) {
            return ((NumberExpression) e).value;
        }

        if (e instanceof VariableExpression) {
            return ((VariableExpression) e).name;
        }

        BinaryExpression b = (BinaryExpression) e;

        return "(" + expression(b.left) + " "
                + b.operator + " "
                + expression(b.right) + ")";
    }

    void statements(ArrayList<Statement> list,
                    StringBuilder out, int depth) {

        for (Statement s : list) {

            if (s instanceof LetStatement) {

                LetStatement let = (LetStatement) s;

                out.append(indent(depth))
                        .append("int ").append(let.variableName)
                        .append(" = ").append(expression(let.value))
                        .append(";\n");
            }

            else {

                Expression value = ((PrintStatement) s).expression;

                out.append(indent(depth))
                        .append("System.out.println(")
                        .append(expression(value))
                        .append(");\n");
            }
        }
    }

    String generate(ArrayList<Statement> program) {

        StringBuilder out = new StringBuilder();

        out.append("class GeneratedProgram {\n");
        out.append("    public static void main(String[] args) {\n");

        statements(program, out, 2);

        out.append("    }\n");
        out.append("}\n");

        return out.toString();
    }
}
