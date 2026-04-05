package ast.expressions;

import java.util.List;

public class CallExprNode extends ExpressionNode {
    private final String callee;
    private final List<ExpressionNode> arguments;

    public CallExprNode(String callee, List<ExpressionNode> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

    public String getCallee() { return callee; }
    public List<ExpressionNode> getArguments() { return arguments; }
}