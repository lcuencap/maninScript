package ast;

import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<GlobalElementNode> globalElements;

    public ProgramNode(List<GlobalElementNode> globalElements) {
        this.globalElements = globalElements;
    }

    public List<GlobalElementNode> getGlobalElements() {
        return globalElements;
    }
}
