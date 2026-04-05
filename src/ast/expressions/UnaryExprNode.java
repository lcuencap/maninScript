package ast.expressions;

public class UnaryExprNode extends ExpressionNode {
    private final String operator;
    private final ExpressionNode right;

    public UnaryExprNode(String operator, ExpressionNode right) {
        this.operator = operator;
        this.right = right;
    }

    public String getOperator() { return operator; }
    public ExpressionNode getRight() { return right; }
}