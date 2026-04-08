package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public void define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            throw new RuntimeException("Variable ya declarada: " + symbol.getName());
        }
        symbols.put(symbol.getName(), symbol);
    }

    public Symbol resolve(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        throw new RuntimeException("Variable no declarada: " + name);
    }
}