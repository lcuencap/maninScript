package ast.expressions;

public class IdentifierExprNode extends ExpressionNode {
    private final String name;

    public IdentifierExprNode(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}