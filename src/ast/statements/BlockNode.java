package ast.statements;

import java.util.List;

public class BlockNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}