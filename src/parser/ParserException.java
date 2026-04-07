package parser;

import lexer.Token;

public class ParserException extends RuntimeException {
    private final int line;
    private final int column;
    private final String foundLexeme;
    private final String expected;

    public ParserException(Token token, String expected) {
        super(buildMessage(token, expected));
        this.line = token.getLine();
        this.column = token.getColumn();
        this.foundLexeme = token.getLexeme();
        this.expected = expected;
    }

    private static String buildMessage(Token token, String expected) {
        return "Error sintáctico en línea " + token.getLine()
                + ", columna " + token.getColumn()
                + ": se esperaba " + expected
                + ". Token encontrado: '" + token.getLexeme() + "'.";
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getFoundLexeme() { return foundLexeme; }
    public String getExpected() { return expected; }
}
