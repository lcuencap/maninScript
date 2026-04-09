package semantic;

public class Symbol {
    private final String name;
    private final String type;
    private final boolean isConstant;

    public Symbol(String name, String type, boolean isConstant) {
        this.name = name;
        this.type = type;
        this.isConstant = isConstant;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public boolean isConstant() {
        return isConstant;
    }
}
