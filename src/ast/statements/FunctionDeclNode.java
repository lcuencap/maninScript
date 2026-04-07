package ast.statements;

import java.util.List;
import ast.ASTNode;
import ast.GlobalElement;

public class FunctionDeclNode extends ASTNode implements GlobalElement {
    private final String returnType;
    private final String name;
    private final List<ParameterNode> parameters;
    private final BlockNode body;

    public FunctionDeclNode(String returnType, String name, List<ParameterNode> parameters, BlockNode body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getReturnType() { return returnType; }
    public String getName() { return name; }
    public List<ParameterNode> getParameters() { return parameters; }
    public BlockNode getBody() { return body; }
}
