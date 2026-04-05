package ast.statements;

import ast.expressions.ExpressionNode;

public class IfNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockNode thenBlock;
    private final BlockNode elseBlock;

    public IfNode(ExpressionNode condition, BlockNode thenBlock, BlockNode elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public ExpressionNode getCondition() { return condition; }
    public BlockNode getThenBlock() { return thenBlock; }
    public BlockNode getElseBlock() { return elseBlock; }
}
