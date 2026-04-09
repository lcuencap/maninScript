package semantic;

import ast.ASTNode;
import ast.GlobalElement;
import ast.ProgramNode;
import ast.expressions.BinaryExprNode;
import ast.expressions.CallExprNode;
import ast.expressions.ExpressionNode;
import ast.expressions.IdentifierExprNode;
import ast.expressions.LiteralExprNode;
import ast.expressions.UnaryExprNode;
import ast.statements.AssignmentNode;
import ast.statements.BlockNode;
import ast.statements.DeclarationNode;
import ast.statements.FunctionDeclNode;
import ast.statements.IfNode;
import ast.statements.ParameterNode;
import ast.statements.PrintNode;
import ast.statements.ReturnNode;
import ast.statements.StatementNode;
import ast.statements.WhileNode;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {
    private SymbolTable globalScope = new SymbolTable(null);
    private SymbolTable currentScope = globalScope;
    private final Map<String, FunctionSymbol> functions = new HashMap<>();
    private FunctionDeclNode currentFunction;
    private Symbol symbolBeingInitialized;

    public void analyze(ProgramNode program) {
        globalScope = new SymbolTable(null);
        currentScope = globalScope;
        functions.clear();
        currentFunction = null;
        symbolBeingInitialized = null;

        registerFunctions(program);
        registerGlobalVariables(program);
        analyzeGlobalInitializers(program);
        analyzeFunctions(program);
    }

    private void registerFunctions(ProgramNode program) {
        for (GlobalElement element : program.getGlobalElements()) {
            if (!(element instanceof FunctionDeclNode function)) {
                continue;
            }

            if (functions.containsKey(function.getName())) {
                throw error(function, "Function already declared: " + function.getName());
            }

            List<String> parameterTypes = new ArrayList<>();
            for (ParameterNode parameter : function.getParameters()) {
                parameterTypes.add(parameter.getTypeName());
            }

            functions.put(function.getName(), new FunctionSymbol(
                    function.getName(),
                    function.getReturnType(),
                    parameterTypes
            ));
        }
    }

    private void registerGlobalVariables(ProgramNode program) {
        for (GlobalElement element : program.getGlobalElements()) {
            if (!(element instanceof DeclarationNode declaration)) {
                continue;
            }

            ensureVariableNameAvailable(globalScope, declaration, declaration.getIdentifier());
            globalScope.define(new Symbol(
                    declaration.getIdentifier(),
                    declaration.getTypeName(),
                    declaration.isConstant()
            ));
        }
    }

    private void analyzeGlobalInitializers(ProgramNode program) {
        currentScope = globalScope;

        for (GlobalElement element : program.getGlobalElements()) {
            if (element instanceof DeclarationNode declaration) {
                Symbol symbol = globalScope.resolve(declaration.getIdentifier());
                analyzeDeclarationInitializer(declaration, symbol);
            }
        }
    }

    private void analyzeFunctions(ProgramNode program) {
        for (GlobalElement element : program.getGlobalElements()) {
            if (element instanceof FunctionDeclNode function) {
                analyzeFunction(function);
            }
        }
    }

    private void analyzeFunction(FunctionDeclNode function) {
        SymbolTable previousScope = currentScope;
        FunctionDeclNode previousFunction = currentFunction;

        currentScope = new SymbolTable(globalScope);
        currentFunction = function;

        try {
            for (ParameterNode parameter : function.getParameters()) {
                if (currentScope.isDefinedLocally(parameter.getIdentifier())) {
                    throw error(parameter, "Duplicate parameter: " + parameter.getIdentifier());
                }

                currentScope.define(new Symbol(
                        parameter.getIdentifier(),
                        parameter.getTypeName(),
                        false
                ));
            }

            analyzeBlockContents(function.getBody());

            if (!"naDeNa".equals(function.getReturnType()) && !alwaysReturns(function.getBody())) {
                throw error(function, "Function '" + function.getName() + "' must return a value of type " + function.getReturnType());
            }
        } finally {
            currentScope = previousScope;
            currentFunction = previousFunction;
        }
    }

    private void analyzeNode(ASTNode node) {
        if (node instanceof DeclarationNode declaration) {
            analyzeLocalDeclaration(declaration);
            return;
        }

        if (node instanceof AssignmentNode assignment) {
            Symbol symbol = resolveVariable(assignment.getIdentifier(), assignment);
            if (symbol.isConstant()) {
                throw error(assignment, "Cannot modify constant '" + assignment.getIdentifier() + "'");
            }

            String expressionType = analyzeExpression(assignment.getExpression());
            ensureAssignable(symbol.getType(), expressionType, assignment,
                    "Cannot assign value of type " + expressionType + " to '" + assignment.getIdentifier() + "' of type " + symbol.getType());
            return;
        }

        if (node instanceof BlockNode block) {
            analyzeBlock(block);
            return;
        }

        if (node instanceof IfNode ifNode) {
            String conditionType = analyzeExpression(ifNode.getCondition());
            ensureType("jura", conditionType, ifNode.getCondition(), "If condition must be of type jura");
            analyzeBlock(ifNode.getThenBlock());

            if (ifNode.getElseBlock() != null) {
                analyzeBlock(ifNode.getElseBlock());
            }
            return;
        }

        if (node instanceof WhileNode whileNode) {
            String conditionType = analyzeExpression(whileNode.getCondition());
            ensureType("jura", conditionType, whileNode.getCondition(), "While condition must be of type jura");
            analyzeBlock(whileNode.getBody());
            return;
        }

        if (node instanceof PrintNode printNode) {
            for (ExpressionNode argument : printNode.getArguments()) {
                String argumentType = analyzeExpression(argument);
                if ("naDeNa".equals(argumentType)) {
                    throw error(argument, "Cannot use a value of type naDeNa in 'di!'");
                }
            }
            return;
        }

        if (node instanceof ReturnNode returnNode) {
            analyzeReturn(returnNode);
            return;
        }

        if (node instanceof FunctionDeclNode functionNode) {
            analyzeFunction(functionNode);
        }
    }

    private void analyzeLocalDeclaration(DeclarationNode declaration) {
        ensureVariableNameAvailable(currentScope, declaration, declaration.getIdentifier());

        Symbol symbol = new Symbol(
                declaration.getIdentifier(),
                declaration.getTypeName(),
                declaration.isConstant()
        );
        currentScope.define(symbol);
        analyzeDeclarationInitializer(declaration, symbol);
    }

    private void analyzeDeclarationInitializer(DeclarationNode declaration, Symbol symbol) {
        if (declaration.getInitializer() == null) {
            return;
        }

        Symbol previousInitializing = symbolBeingInitialized;
        symbolBeingInitialized = symbol;

        try {
            String initializerType = analyzeExpression(declaration.getInitializer());
            ensureAssignable(symbol.getType(), initializerType, declaration.getInitializer(),
                    "Cannot initialize '" + declaration.getIdentifier() + "' of type " + symbol.getType()
                            + " with a value of type " + initializerType);
        } finally {
            symbolBeingInitialized = previousInitializing;
        }
    }

    private void analyzeBlock(BlockNode block) {
        SymbolTable previousScope = currentScope;
        currentScope = new SymbolTable(previousScope);

        try {
            analyzeBlockContents(block);
        } finally {
            currentScope = previousScope;
        }
    }

    private void analyzeBlockContents(BlockNode block) {
        for (StatementNode statement : block.getStatements()) {
            analyzeNode(statement);
        }
    }

    private void analyzeReturn(ReturnNode returnNode) {
        if (currentFunction == null) {
            throw error(returnNode, "'suelta' can only appear inside a function");
        }

        String expectedType = currentFunction.getReturnType();
        ExpressionNode returnedExpression = returnNode.getExpression();

        if ("naDeNa".equals(expectedType)) {
            if (returnedExpression != null) {
                throw error(returnedExpression, "Function '" + currentFunction.getName() + "' returns naDeNa and cannot return a value");
            }
            return;
        }

        if (returnedExpression == null) {
            throw error(returnNode, "Function '" + currentFunction.getName() + "' must return a value of type " + expectedType);
        }

        String actualType = analyzeExpression(returnedExpression);
        ensureAssignable(expectedType, actualType, returnedExpression,
                "Function '" + currentFunction.getName() + "' must return " + expectedType + ", but found " + actualType);
    }

    private String analyzeExpression(ExpressionNode expression) {
        if (expression instanceof IdentifierExprNode identifier) {
            Symbol symbol = resolveVariable(identifier.getName(), identifier);
            if (symbol == symbolBeingInitialized) {
                throw error(identifier, "Variable '" + identifier.getName() + "' cannot be used in its own initializer");
            }
            return symbol.getType();
        }

        if (expression instanceof LiteralExprNode literal) {
            return typeOfLiteral(literal);
        }

        if (expression instanceof UnaryExprNode unary) {
            String rightType = analyzeExpression(unary.getRight());
            if (!isNumeric(rightType)) {
                throw error(unary, "Operator '" + unary.getOperator() + "' requires a numeric operand");
            }
            return rightType;
        }

        if (expression instanceof BinaryExprNode binary) {
            String leftType = analyzeExpression(binary.getLeft());
            String rightType = analyzeExpression(binary.getRight());

            return switch (binary.getOperator()) {
                case "+", "-", "*", "/" -> analyzeArithmetic(binary, leftType, rightType);
                case "<", ">", "<=", ">=" -> analyzeRelational(binary, leftType, rightType);
                case "==" -> analyzeEquality(binary, leftType, rightType);
                case "y", "o" -> analyzeLogical(binary, leftType, rightType);
                default -> throw error(binary, "Unknown operator '" + binary.getOperator() + "'");
            };
        }

        if (expression instanceof CallExprNode call) {
            FunctionSymbol function = functions.get(call.getCallee());
            if (function == null) {
                throw error(call, "Function not declared: " + call.getCallee());
            }

            if (call.getArguments().size() != function.getParameterTypes().size()) {
                throw error(call, "Function '" + call.getCallee() + "' expects "
                        + function.getParameterTypes().size() + " arguments, but got " + call.getArguments().size());
            }

            for (int i = 0; i < call.getArguments().size(); i++) {
                ExpressionNode argument = call.getArguments().get(i);
                String argumentType = analyzeExpression(argument);
                String parameterType = function.getParameterTypes().get(i);

                ensureAssignable(parameterType, argumentType, argument,
                        "Argument " + (i + 1) + " of function '" + call.getCallee()
                                + "' must be " + parameterType + ", but found " + argumentType);
            }

            return function.getReturnType();
        }

        throw error(expression, "Unsupported expression node: " + expression.getClass().getSimpleName());
    }

    private String analyzeArithmetic(ASTNode node, String leftType, String rightType) {
        if (!isNumeric(leftType) || !isNumeric(rightType)) {
            throw error(node, "Arithmetic operators require numeric operands, but found " + leftType + " and " + rightType);
        }

        return promoteNumeric(leftType, rightType);
    }

    private String analyzeRelational(ASTNode node, String leftType, String rightType) {
        if (!isNumeric(leftType) || !isNumeric(rightType)) {
            throw error(node, "Comparison operators require numeric operands, but found " + leftType + " and " + rightType);
        }

        return "jura";
    }

    private String analyzeEquality(ASTNode node, String leftType, String rightType) {
        if (!areComparable(leftType, rightType)) {
            throw error(node, "Operator '==' cannot compare values of type " + leftType + " and " + rightType);
        }

        return "jura";
    }

    private String analyzeLogical(ASTNode node, String leftType, String rightType) {
        if (!"jura".equals(leftType) || !"jura".equals(rightType)) {
            throw error(node, "Logical operators require operands of type jura, but found " + leftType + " and " + rightType);
        }

        return "jura";
    }

    private void ensureVariableNameAvailable(SymbolTable scope, ASTNode node, String name) {
        if (scope.isDefinedLocally(name)) {
            throw error(node, "Variable already declared in this scope: " + name);
        }

        if (scope == globalScope && functions.containsKey(name)) {
            throw error(node, "Name '" + name + "' is already used by a function");
        }
    }

    private Symbol resolveVariable(String name, ASTNode node) {
        Symbol symbol = currentScope.resolve(name);
        if (symbol == null) {
            throw error(node, "Variable not declared: " + name);
        }
        return symbol;
    }

    private void ensureAssignable(String expectedType, String actualType, ASTNode node, String message) {
        if (!isAssignable(expectedType, actualType)) {
            throw error(node, message);
        }
    }

    private void ensureType(String expectedType, String actualType, ASTNode node, String message) {
        if (!expectedType.equals(actualType)) {
            throw error(node, message + " (found " + actualType + ")");
        }
    }

    private boolean isAssignable(String expectedType, String actualType) {
        if (expectedType.equals(actualType)) {
            return true;
        }

        return "aPachas".equals(expectedType) && "pavos".equals(actualType);
    }

    private boolean areComparable(String leftType, String rightType) {
        if (leftType.equals(rightType)) {
            return true;
        }

        return isNumeric(leftType) && isNumeric(rightType);
    }

    private boolean isNumeric(String type) {
        return "pavos".equals(type) || "aPachas".equals(type);
    }

    private String promoteNumeric(String leftType, String rightType) {
        if ("aPachas".equals(leftType) || "aPachas".equals(rightType)) {
            return "aPachas";
        }
        return "pavos";
    }

    private String typeOfLiteral(LiteralExprNode literal) {
        return switch (literal.getLiteralType()) {
            case INT_LITERAL -> "pavos";
            case FLOAT_LITERAL -> "aPachas";
            case CHAR_LITERAL -> "carro";
            case STRING_LITERAL -> "letrilla";
            case JURAO, BULO -> "jura";
            default -> throw error(literal, "Unsupported literal token: " + literal.getLiteralType());
        };
    }

    private boolean alwaysReturns(BlockNode block) {
        for (StatementNode statement : block.getStatements()) {
            if (alwaysReturns(statement)) {
                return true;
            }
        }
        return false;
    }

    private boolean alwaysReturns(StatementNode statement) {
        if (statement instanceof ReturnNode) {
            return true;
        }

        if (statement instanceof BlockNode block) {
            return alwaysReturns(block);
        }

        if (statement instanceof IfNode ifNode) {
            return ifNode.getElseBlock() != null
                    && alwaysReturns(ifNode.getThenBlock())
                    && alwaysReturns(ifNode.getElseBlock());
        }

        return false;
    }

    private SemanticException error(ASTNode node, String message) {
        return new SemanticException(node, message);
    }
}
