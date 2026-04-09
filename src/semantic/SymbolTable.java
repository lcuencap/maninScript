package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public boolean isDefinedLocally(String name) {
        return symbols.containsKey(name);
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    public Symbol resolve(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }
}
