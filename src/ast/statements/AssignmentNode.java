package ast.statements;
import ast.expressions.ExpressionNode;

public class AssignmentNode extends StatementNode {
    private final String identifier;
    private final ExpressionNode expression;

    public AssignmentNode(String identifier, ExpressionNode expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    public String getIdentifier() { return identifier; }
    public ExpressionNode getExpression() { return expression; }
}
