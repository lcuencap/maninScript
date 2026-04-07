package ast;

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

public final class ASTPrinter {

    private ASTPrinter() {}

    public static String print(ASTNode root) {
        StringBuilder sb = new StringBuilder();
        append(sb, root, 0);
        return sb.toString();
    }

    private static void append(StringBuilder sb, ASTNode node, int indent) {
        indent(sb, indent);

        if (node == null) {
            sb.append("<null>\n");
            return;
        }

        if (node instanceof ProgramNode p) {
            sb.append("Program\n");
            for (GlobalElement ge : p.getGlobalElements()) {
                append(sb, (ASTNode) ge, indent + 1);
            }
            return;
        }

        if (node instanceof FunctionDeclNode f) {
            sb.append("FunctionDecl name=").append(f.getName())
                    .append(" returnType=").append(f.getReturnType()).append("\n");
            for (ParameterNode param : f.getParameters()) {
                append(sb, param, indent + 1);
            }
            append(sb, f.getBody(), indent + 1);
            return;
        }

        if (node instanceof ParameterNode p) {
            sb.append("Param type=").append(p.getTypeName())
                    .append(" name=").append(p.getIdentifier()).append("\n");
            return;
        }

        if (node instanceof BlockNode b) {
            sb.append("Block\n");
            for (StatementNode s : b.getStatements()) {
                append(sb, s, indent + 1);
            }
            return;
        }

        if (node instanceof DeclarationNode d) {
            sb.append(d.isConstant() ? "ConstDecl" : "VarDecl")
                    .append(" type=").append(d.getTypeName())
                    .append(" name=").append(d.getIdentifier()).append("\n");
            if (d.getInitializer() != null) {
                indent(sb, indent + 1);
                sb.append("init:\n");
                append(sb, d.getInitializer(), indent + 2);
            }
            return;
        }

        if (node instanceof AssignmentNode a) {
            sb.append("Assign name=").append(a.getIdentifier()).append("\n");
            indent(sb, indent + 1);
            sb.append("value:\n");
            append(sb, a.getExpression(), indent + 2);
            return;
        }

        if (node instanceof IfNode ifNode) {
            sb.append("If\n");
            indent(sb, indent + 1);
            sb.append("cond:\n");
            append(sb, ifNode.getCondition(), indent + 2);
            indent(sb, indent + 1);
            sb.append("then:\n");
            append(sb, ifNode.getThenBlock(), indent + 2);
            if (ifNode.getElseBlock() != null) {
                indent(sb, indent + 1);
                sb.append("else:\n");
                append(sb, ifNode.getElseBlock(), indent + 2);
            }
            return;
        }

        if (node instanceof WhileNode w) {
            sb.append("While\n");
            indent(sb, indent + 1);
            sb.append("cond:\n");
            append(sb, w.getCondition(), indent + 2);
            indent(sb, indent + 1);
            sb.append("body:\n");
            append(sb, w.getBody(), indent + 2);
            return;
        }

        if (node instanceof ReturnNode r) {
            sb.append("Return\n");
            if (r.getExpression() != null) {
                append(sb, r.getExpression(), indent + 1);
            }
            return;
        }

        if (node instanceof PrintNode pn) {
            sb.append("Print args=").append(pn.getArguments().size()).append("\n");
            for (ExpressionNode arg : pn.getArguments()) {
                append(sb, arg, indent + 1);
            }
            return;
        }

        if (node instanceof BinaryExprNode be) {
            sb.append("Binary ").append(be.getOperator()).append("\n");
            append(sb, be.getLeft(), indent + 1);
            append(sb, be.getRight(), indent + 1);
            return;
        }

        if (node instanceof UnaryExprNode u) {
            sb.append("Unary ").append(u.getOperator()).append("\n");
            append(sb, u.getRight(), indent + 1);
            return;
        }

        if (node instanceof CallExprNode c) {
            sb.append("Call callee=").append(c.getCallee())
                    .append(" args=").append(c.getArguments().size()).append("\n");
            for (ExpressionNode arg : c.getArguments()) {
                append(sb, arg, indent + 1);
            }
            return;
        }

        if (node instanceof LiteralExprNode l) {
            sb.append("Literal ").append(l.getValue()).append("\n");
            return;
        }

        if (node instanceof IdentifierExprNode id) {
            sb.append("Identifier ").append(id.getName()).append("\n");
            return;
        }

        sb.append("UnknownNode(").append(node.getClass().getSimpleName()).append(")\n");
    }

    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }
}
