package ast.statements;

import ast.expressions.ExpressionNode;

public class WhileNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockNode body;

    public WhileNode(ExpressionNode condition, BlockNode body) {
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() { return condition; }
    public BlockNode getBody() { return body; }
}
