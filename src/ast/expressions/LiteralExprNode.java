package ast.expressions;

public class LiteralExprNode extends ExpressionNode {
    private final String value;

    public LiteralExprNode(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}