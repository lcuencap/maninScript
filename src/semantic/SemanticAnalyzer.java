package semantic;

import ast.*;
import ast.expressions.*;
import ast.statements.*;

public class SemanticAnalyzer {

    private SymbolTable currentScope = new SymbolTable(null);

    public void analyze(ProgramNode program) {
        for (GlobalElement element : program.getGlobalElements()) {
            if (element instanceof ASTNode node) {
                analyzeNode(node);
            }
        }
    }

    private void analyzeNode(ASTNode node) {

        // ========================
        // DECLARACIONES
        // ========================
        if (node instanceof DeclarationNode d) {
            Symbol symbol = new Symbol(
                    d.getIdentifier(),
                    d.getTypeName(),
                    d.isConstant()
            );

            currentScope.define(symbol);

            if (d.getInitializer() != null) {
                analyzeExpression(d.getInitializer());
            }
        }

        // ========================
        // ASIGNACIONES
        // ========================
        else if (node instanceof AssignmentNode a) {
            Symbol symbol = currentScope.resolve(a.getIdentifier());

            if (symbol.isConstant()) {
                throw new RuntimeException("No puedes modificar una constante: " + a.getIdentifier());
            }

            analyzeExpression(a.getExpression());
        }

        // ========================
        // BLOQUE
        // ========================
        else if (node instanceof BlockNode b) {
            SymbolTable previous = currentScope;
            currentScope = new SymbolTable(previous);

            for (StatementNode stmt : b.getStatements()) {
                analyzeNode(stmt);
            }

            currentScope = previous;
        }

        // ========================
        // IF
        // ========================
        else if (node instanceof IfNode i) {
            analyzeExpression(i.getCondition());
            analyzeNode(i.getThenBlock());

            if (i.getElseBlock() != null) {
                analyzeNode(i.getElseBlock());
            }
        }

        // ========================
        // WHILE
        // ========================
        else if (node instanceof WhileNode w) {
            analyzeExpression(w.getCondition());
            analyzeNode(w.getBody());
        }

        // ========================
        // FUNCIÓN
        // ========================
        else if (node instanceof FunctionDeclNode f) {
            SymbolTable previous = currentScope;
            currentScope = new SymbolTable(previous);

            for (ParameterNode param : f.getParameters()) {
                currentScope.define(new Symbol(
                        param.getIdentifier(),
                        param.getTypeName(),
                        false
                ));
            }

            analyzeNode(f.getBody());

            currentScope = previous;
        }

        // ========================
        // PRINT
        // ========================
        else if (node instanceof PrintNode p) {
            for (ExpressionNode arg : p.getArguments()) {
                analyzeExpression(arg);
            }
        }

        // ========================
        // RETURN
        // ========================
        else if (node instanceof ReturnNode r) {
            if (r.getExpression() != null) {
                analyzeExpression(r.getExpression());
            }
        }
    }

    // ========================
    // EXPRESIONES
    // ========================
    private void analyzeExpression(ExpressionNode expr) {

        if (expr instanceof IdentifierExprNode id) {
            currentScope.resolve(id.getName());
        }

        else if (expr instanceof BinaryExprNode b) {
            analyzeExpression(b.getLeft());
            analyzeExpression(b.getRight());
        }

        else if (expr instanceof UnaryExprNode u) {
            analyzeExpression(u.getRight());
        }

        else if (expr instanceof CallExprNode c) {
            for (ExpressionNode arg : c.getArguments()) {
                analyzeExpression(arg);
            }
        }

        else if (expr instanceof LiteralExprNode) {
            // todo bien
        }
    }
}