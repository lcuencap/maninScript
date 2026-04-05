package ast.statements;

import ast.expressions.ExpressionNode;

public class DeclarationNode extends StatementNode implements ast.GlobalElement {
    private final boolean constant;
    private final String typeName;
    private final String identifier;
    private final ExpressionNode initializer;

    public DeclarationNode(boolean constant, String typeName, String identifier, ExpressionNode initializer) {
        this.constant = constant;
        this.typeName = typeName;
        this.identifier = identifier;
        this.initializer = initializer;
    }

    public boolean isConstant() { return constant; }
    public String getTypeName() { return typeName; }
    public String getIdentifier() { return identifier; }
    public ExpressionNode getInitializer() { return initializer; }
}