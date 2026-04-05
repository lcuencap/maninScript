package ast.statements;

import java.util.List;
import ast.expressions.ExpressionNode;

public class PrintNode extends StatementNode {
    private final List<ExpressionNode> arguments;

    public PrintNode(List<ExpressionNode> arguments) {
        this.arguments = arguments;
    }

    public List<ExpressionNode> getArguments() { return arguments; }
}