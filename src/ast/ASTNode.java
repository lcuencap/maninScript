package ast;

public abstract class ASTNode {
    private int line = -1;
    private int column = -1;

    public int getLine() { return line; }
    public int getColumn() { return column; }

    public void setPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
