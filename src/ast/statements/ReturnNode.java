package ast.statements;

import ast.expressions.ExpressionNode;

public class ReturnNode extends StatementNode {
    private final ExpressionNode expression;

    public ReturnNode(ExpressionNode expression) {
        this.expression = expression;
    }

    public ExpressionNode getExpression() { return expression; }
}