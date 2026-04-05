package ast.statements;

public class ParameterNode {
    private final String typeName;
    private final String identifier;

    public ParameterNode(String typeName, String identifier) {
        this.typeName = typeName;
        this.identifier = identifier;
    }

    public String getTypeName() { return typeName; }
    public String getIdentifier() { return identifier; }
}