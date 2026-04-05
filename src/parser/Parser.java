package parser;

import java.util.ArrayList;
import java.util.List;

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
import lexer.Token;
import lexer.TokenType;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parseProgram() {
        List<GlobalElement> globals = new ArrayList<>();

        while (!isAtEnd()) {
            globals.add(parseGlobalElement());
        }

        consume(TokenType.EOF, "S'esperava final de fitxer.");
        return new ProgramNode(globals);
    }

    private GlobalElement parseGlobalElement() {
        if (check(TokenType.LA_FAENA)) {
            return parseFunctionDecl();
        }

        if (check(TokenType.UN_TAL) || check(TokenType.A_MUERTE)) {
            DeclarationNode decl = parseDeclaration();
            consume(TokenType.SEMICOLON, "S'esperava ';' després de la declaració global.");
            return decl;
        }

        throw error(peek(), "S'esperava una declaració global o una funció.");
    }

    private BlockNode parseBlock() {
        consume(TokenType.LBRACE, "S'esperava '{'.");
        List<StatementNode> statements = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.RBRACE, "S'esperava '}'.");
        return new BlockNode(statements);
    }

    private StatementNode parseStatement() {
        if (check(TokenType.UN_TAL) || check(TokenType.A_MUERTE)) {
            DeclarationNode decl = parseDeclaration();
            consume(TokenType.SEMICOLON, "S'esperava ';' després de la declaració.");
            return decl;
        }

        if (check(TokenType.IDENTIFIER)) {
            AssignmentNode assignment = parseAssignment();
            consume(TokenType.SEMICOLON, "S'esperava ';' després de l'assignació.");
            return assignment;
        }

        if (check(TokenType.FIJATE_SI)) return parseIfStatement();
        if (check(TokenType.SEGUN_VEA)) return parseWhileStatement();

        if (check(TokenType.SUELTA)) {
            ReturnNode ret = parseReturnStatement();
            consume(TokenType.SEMICOLON, "S'esperava ';' després del return.");
            return ret;
        }

        if (check(TokenType.DI)) {
            PrintNode print = parsePrintStatement();
            consume(TokenType.SEMICOLON, "S'esperava ';' després del print.");
            return print;
        }

        throw error(peek(), "Sentència no vàlida.");
    }

    private DeclarationNode parseDeclaration() {
        boolean constant;

        if (match(TokenType.UN_TAL)) {
            constant = false;
        } else if (match(TokenType.A_MUERTE)) {
            constant = true;
        } else {
            throw error(peek(), "S'esperava 'unTal' o 'aMuerte'.");
        }

        String type = parseType();
        Token id = consume(TokenType.IDENTIFIER, "S'esperava identificador.");

        ExpressionNode initializer = null;

        if (constant) {
            consume(TokenType.ASSIGN, "Una constant ha de tenir inicialització.");
            initializer = parseExpression();
        } else if (match(TokenType.ASSIGN)) {
            initializer = parseExpression();
        }

        return new DeclarationNode(constant, type, id.getLexeme(), initializer);
    }

    private AssignmentNode parseAssignment() {
        Token id = consume(TokenType.IDENTIFIER, "S'esperava identificador.");
        consume(TokenType.ASSIGN, "S'esperava '='.");
        ExpressionNode expr = parseExpression();
        return new AssignmentNode(id.getLexeme(), expr);
    }

    private PrintNode parsePrintStatement() {
        consume(TokenType.DI, "S'esperava 'di!'.");
        consume(TokenType.LPAREN, "S'esperava '('.");

        List<ExpressionNode> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpression());
            while (match(TokenType.COMMA)) {
                args.add(parseExpression());
            }
        }

        consume(TokenType.RPAREN, "S'esperava ')'.");
        return new PrintNode(args);
    }

    private IfNode parseIfStatement() {
        consume(TokenType.FIJATE_SI, "S'esperava 'fijateSi'.");
        consume(TokenType.LPAREN, "S'esperava '(' després de fijateSi.");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RPAREN, "S'esperava ')' després de la condició.");

        BlockNode thenBlock = parseBlock();
        BlockNode elseBlock = null;

        if (match(TokenType.Y_SI_NO)) {
            elseBlock = parseBlock();
        }

        return new IfNode(condition, thenBlock, elseBlock);
    }

    private WhileNode parseWhileStatement() {
        consume(TokenType.SEGUN_VEA, "S'esperava 'segunVea'.");
        consume(TokenType.LPAREN, "S'esperava '(' després de segunVea.");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RPAREN, "S'esperava ')' després de la condició.");
        BlockNode body = parseBlock();

        return new WhileNode(condition, body);
    }

    private FunctionDeclNode parseFunctionDecl() {
        consume(TokenType.LA_FAENA, "S'esperava 'laFaena'.");

        String returnType = match(TokenType.NA_DE_NA) ? "naDeNa" : parseType();

        Token name = consume(TokenType.IDENTIFIER, "S'esperava el nom de la funció.");
        consume(TokenType.LPAREN, "S'esperava '('.");

        List<ParameterNode> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(parseParameter());
            while (match(TokenType.COMMA)) {
                params.add(parseParameter());
            }
        }

        consume(TokenType.RPAREN, "S'esperava ')'.");
        BlockNode body = parseBlock();

        return new FunctionDeclNode(returnType, name.getLexeme(), params, body);
    }

    private ParameterNode parseParameter() {
        String type = parseType();
        Token id = consume(TokenType.IDENTIFIER, "S'esperava identificador de paràmetre.");
        return new ParameterNode(type, id.getLexeme());
    }

    private ReturnNode parseReturnStatement() {
        consume(TokenType.SUELTA, "S'esperava 'suelta'.");
        ExpressionNode value = null;

        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }

        return new ReturnNode(value);
    }

    private String parseType() {
        if (match(TokenType.PAVOS)) return "pavos";
        if (match(TokenType.APACHAS)) return "aPachas";
        if (match(TokenType.CARRO)) return "carro";
        if (match(TokenType.JURA)) return "jura";
        if (match(TokenType.LETRILLA)) return "letrilla";

        throw error(peek(), "S'esperava un tipus vàlid.");
    }

    private ExpressionNode parseExpression() {
        return parseOr();
    }

    private ExpressionNode parseOr() {
        ExpressionNode expr = parseAnd();

        while (match(TokenType.OR)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseAnd();
            expr = new BinaryExprNode(expr, op, right);
        }
        return expr;
    }

    private ExpressionNode parseAnd() {
        ExpressionNode expr = parseComparison();

        while (match(TokenType.AND)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseComparison();
            expr = new BinaryExprNode(expr, op, right);
        }
        return expr;
    }

    private ExpressionNode parseComparison() {
        ExpressionNode expr = parseAddition();

        while (match(TokenType.EQUAL_EQUAL, TokenType.LESS, TokenType.GREATER,
                TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseAddition();
            expr = new BinaryExprNode(expr, op, right);
        }
        return expr;
    }

    private ExpressionNode parseAddition() {
        ExpressionNode expr = parseMultiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseMultiplication();
            expr = new BinaryExprNode(expr, op, right);
        }
        return expr;
    }

    private ExpressionNode parseMultiplication() {
        ExpressionNode expr = parseUnary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseUnary();
            expr = new BinaryExprNode(expr, op, right);
        }
        return expr;
    }

    private ExpressionNode parseUnary() {
        if (match(TokenType.PLUS, TokenType.MINUS)) {
            String op = previous().getLexeme();
            ExpressionNode right = parseUnary();
            return new UnaryExprNode(op, right);
        }
        return parsePrimary();
    }

    private ExpressionNode parsePrimary() {
        if (match(TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.STRING_LITERAL,
                TokenType.JURAO, TokenType.BULO)) {
            return new LiteralExprNode(previous().getLexeme());
        }

        if (match(TokenType.IDENTIFIER)) {
            Token id = previous();

            if (match(TokenType.LPAREN)) {
                List<ExpressionNode> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    args.add(parseExpression());
                    while (match(TokenType.COMMA)) {
                        args.add(parseExpression());
                    }
                }
                consume(TokenType.RPAREN, "S'esperava ')' en la crida a funció.");
                return new CallExprNode(id.getLexeme(), args);
            }

            return new IdentifierExprNode(id.getLexeme());
        }

        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = parseExpression();
            consume(TokenType.RPAREN, "S'esperava ')'.");
            return expr;
        }

        throw error(peek(), "S'esperava literal, identificador o expressió entre parèntesis.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParserException error(Token token, String message) {
        return new ParserException(
                "Error sintàctic a línia " + token.getLine() +
                ", columna " + token.getColumn() +
                ": " + message +
                " Token trobat: '" + token.getLexeme() + "'"
        );
    }
}