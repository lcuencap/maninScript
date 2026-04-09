package semantic;

import java.util.List;

public class FunctionSymbol {
    private final String name;
    private final String returnType;
    private final List<String> parameterTypes;

    public FunctionSymbol(String name, String returnType, List<String> parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = List.copyOf(parameterTypes);
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }
}
