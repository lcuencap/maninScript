package ast.statements;

import ast.ASTNode;

public class ParameterNode extends ASTNode {
    private final String typeName;
    private final String identifier;

    public ParameterNode(String typeName, String identifier) {
        this.typeName = typeName;
        this.identifier = identifier;
    }

    public String getTypeName() { return typeName; }
    public String getIdentifier() { return identifier; }
}