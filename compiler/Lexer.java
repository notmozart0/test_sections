import java.util.ArrayList;

public class Lexer {

    // Token class
    public static class Token {

        int number;
        int line;
        int column;

        String lexeme;
        String type;
        String description;

        Token(int number, int line, int column,
              String lexeme, String type, String description) {

            this.number = number;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.type = type;
            this.description = description;
        }
    }

    String source;
    ArrayList<Token> tokens = new ArrayList<>();

    public Lexer(String source) {
        this.source = source;
    }

    // Add token
    void addToken(int line, int column,
                  String lexeme, String type, String description) {

        Token token = new Token(
                tokens.size() + 1,
                line,
                column,
                lexeme,
                type,
                description
        );

        tokens.add(token);
    }

    // Check comments
    boolean isComment(String line, int i) {

        return line.regionMatches(true, i, "yap", 0, 3)
                && (i + 3 == line.length()
                || Character.isWhitespace(line.charAt(i + 3)));
    }

    // Lexer
    public ArrayList<Token> tokenize() {

        tokens.clear();

        String[] lines = source.split("\\R", -1);

        for (int row = 0; row < lines.length; row++) {

            String currentLine = lines[row];

            int i = 0;
            int lineNumber = row + 1;

            boolean hasCode = false;
            boolean hasNL = false;

            while (i < currentLine.length()) {

                char current = currentLine.charAt(i);

                // Ignore spaces
                if (Character.isWhitespace(current)) {
                    i++;
                    continue;
                }

                int column = i + 1;

                // Ignore comments
                if (isComment(currentLine, i)) {
                    break;
                }

                // New line indicator ;)
                if (currentLine.startsWith(";)", i)) {

                    addToken(lineNumber, column,
                            ";)", "NL", "Statement boundary");

                    i += 2;
                    hasNL = true;

                    while (i < currentLine.length()) {

                        if (Character.isWhitespace(currentLine.charAt(i))) {
                            i++;
                        }
                        else if (isComment(currentLine, i)) {
                            break;
                        }
                        else {
                            throw new RuntimeException(
                                    "Unexpected character after ;) at line "
                                    + lineNumber + ", column " + (i + 1)
                            );
                        }
                    }

                    break;
                }

                // Keywords and identifiers
                if (Character.isLetter(current) || current == '_') {

                    int start = i;

                    while (i < currentLine.length()) {

                        char c = currentLine.charAt(i);

                        if (Character.isLetterOrDigit(c) || c == '_') {
                            i++;
                        }
                        else {
                            break;
                        }
                    }

                    String word = currentLine.substring(start, i);

                    hasCode = true;

                    if (word.equalsIgnoreCase("LET")) {

                        addToken(lineNumber, column,
                                word, "LET", "Reserved keyword");

                    }
                    else if (word.equalsIgnoreCase("PRINT")) {

                        addToken(lineNumber, column,
                                word, "PRINT", "Reserved keyword");

                    }
                    else {

                        addToken(lineNumber, column,
                                word, "ID", "Variable name");
                    }

                    continue;
                }

                // Numbers
                if (Character.isDigit(current)) {

                    int start = i;

                    while (i < currentLine.length()
                            && Character.isDigit(currentLine.charAt(i))) {

                        i++;
                    }

                    String number = currentLine.substring(start, i);

                    addToken(lineNumber, column,
                            number, "NUM(" + number + ")",
                            "Integer literal");

                    hasCode = true;
                    continue;
                }

                // Operators
                if (current == '=') {

                    addToken(lineNumber, column,
                            "=", "EQUALS", "Assignment symbol");

                }
                else if (current == '+') {

                    addToken(lineNumber, column,
                            "+", "PLUS", "Addition operator");

                }
                else if (current == '*') {

                    addToken(lineNumber, column,
                            "*", "STAR", "Multiplication operator");

                }
                else {

                    throw new RuntimeException(
                            "Unknown character '" + current +
                            "' at line " + lineNumber +
                            ", column " + column
                    );
                }

                hasCode = true;
                i++;
            }

            // Check ;)
            if (hasCode && !hasNL) {

                throw new RuntimeException(
                        "Missing ;) at the end of line " + lineNumber
                );
            }
        }

        // EOF
        String lastLine = lines[lines.length - 1];

        addToken(
                lines.length,
                lastLine.length() + 1,
                "end-of-file",
                "EOF",
                "End of file"
        );

        return tokens;
    }
}