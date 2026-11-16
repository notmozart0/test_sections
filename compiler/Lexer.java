import java.util.ArrayList;

public class Lexer {

    // ================= TOKEN =================

    public static class Token {

        public int number;
        public int line;
        public int column;

        public String lexeme;
        public String type;
        public String description;

        public Token(int number, int line, int column,
                     String lexeme, String type, String description) {

            this.number = number;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.type = type;
            this.description = description;
        }
    }

    // ================= LEXER =================

    private String source;

    private ArrayList<Token> tokens = new ArrayList<>();

    public Lexer(String source) {
        this.source = source;
    }

    // Create a token and add it to the list
    private void addToken(int line, int column,
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

    // Check if a comment starts here
    private boolean isCommentStart(String line, int position) {

        return line.regionMatches(true, position, "yap", 0, 3)
                && (position + 3 == line.length()
                || Character.isWhitespace(line.charAt(position + 3)));
    }

    // Perform lexical analysis
    public ArrayList<Token> tokenize() {

        tokens.clear();

        String[] lines = source.split("\\R", -1);

        // Process every line
        for (int row = 0; row < lines.length; row++) {

            String currentLine = lines[row];

            int position = 0;
            int lineNumber = row + 1;

            boolean hasCode = false;
            boolean hasNewLineToken = false;

            // Process every character
            while (position < currentLine.length()) {

                char current = currentLine.charAt(position);

                // Ignore spaces and tabs
                if (Character.isWhitespace(current)) {
                    position++;
                    continue;
                }

                int column = position + 1;

                // Ignore comments
                if (isCommentStart(currentLine, position)) {
                    break;
                }

                // Recognize ;)
                if (currentLine.startsWith(";)", position)) {

                    addToken(lineNumber, column,
                            ";)", "NL", "Statement boundary");

                    position = position + 2;

                    hasNewLineToken = true;

                    // Check what comes after ;)
                    while (position < currentLine.length()) {

                        if (Character.isWhitespace(currentLine.charAt(position))) {
                            position++;
                        }
                        else if (isCommentStart(currentLine, position)) {
                            break;
                        }
                        else {
                            throw new RuntimeException(
                                    "Unexpected character after ;) at line "
                                    + lineNumber + ", column " + (position + 1)
                            );
                        }
                    }

                    break;
                }

                // Recognize keywords and identifiers
                if (Character.isLetter(current) || current == '_') {

                    int start = position;

                    while (position < currentLine.length()) {

                        char c = currentLine.charAt(position);

                        if (Character.isLetterOrDigit(c) || c == '_') {
                            position++;
                        }
                        else {
                            break;
                        }
                    }

                    String word = currentLine.substring(start, position);

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

                // Recognize integer numbers
                if (Character.isDigit(current)) {

                    int start = position;

                    while (position < currentLine.length()
                            && Character.isDigit(currentLine.charAt(position))) {

                        position++;
                    }

                    String number = currentLine.substring(start, position);

                    addToken(lineNumber, column,
                            number, "NUM(" + number + ")",
                            "Integer literal");

                    hasCode = true;

                    continue;
                }

                // Recognize operators
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
                position++;
            }

            // Every statement must end with ;)
            if (hasCode && !hasNewLineToken) {

                throw new RuntimeException(
                        "Missing ;) at the end of line " + lineNumber
                );
            }
        }

        // Generate EOF
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