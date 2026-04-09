package semantic;

import ast.ASTNode;

public class SemanticException extends RuntimeException {
    private final int line;
    private final int column;

    public SemanticException(ASTNode node, String message) {
        super(buildMessage(node, message));
        this.line = node == null ? -1 : node.getLine();
        this.column = node == null ? -1 : node.getColumn();
    }

    private static String buildMessage(ASTNode node, String message) {
        if (node == null || node.getLine() < 1 || node.getColumn() < 1) {
            return "Semantic error: " + message;
        }

        return "Semantic error at line " + node.getLine()
                + ", column " + node.getColumn()
                + ": " + message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
