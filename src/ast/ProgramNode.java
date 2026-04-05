package ast;

import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<GlobalElement> globalElements;

    public ProgramNode(List<GlobalElement> globalElements) {
        this.globalElements = globalElements;
    }

    public List<GlobalElement> getGlobalElements() {
        return globalElements;
    }
}
