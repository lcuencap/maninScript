package parser;

import java.util.ArrayList;
import java.util.List;

import lexer.*;
import ast.*;
import ast.statements.StatementNode;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parseProgram() {
        List<GlobalElementNode> globalElements = new ArrayList<>();

        while (!isAtEnd()) {
            globalElements.add(parseGlobalElement());
        }

        consume(TokenType.EOF, "Se esperaba el final del archivo.");
        return new ProgramNode(globalElements);
    }

    private GlobalElementNode parseGlobalElement() {
        if (check(TokenType.LA_FAENA)) {
            return parseFunctionDecl();
        }

        if (check(TokenType.UN_TAL) || check(TokenType.A_MUERTE)) {
            DeclarationNode declaration = parseDeclaration();
            consume(TokenType.SEMICOLON, "Se esperaba ';' después de la declaración global.");
            return declaration;
        }

        throw error(peek(), "Se esperaba una declaración global o una función.");
    }

    private BlockNode parseBlock() {
        consume(TokenType.LBRACE, "Se esperaba '{' al inicio del bloque.");

        List<StatementNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.RBRACE, "Se esperaba '}' al final del bloque.");
        return new BlockNode(statements);
    }

    private StatementNode parseStatement() {
        if (check(TokenType.UN_TAL) || check(TokenType.A_MUERTE)) {
            DeclarationNode declaration = parseDeclaration();
            consume(TokenType.SEMICOLON, "Se esperaba ';' después de la declaración.");
            return declaration;
        }

        if (check(TokenType.IDENTIFIER)) {
            AssignmentNode assignment = parseAssignment();
            consume(TokenType.SEMICOLON, "Se esperaba ';' después de la asignación.");
            return assignment;
        }

        if (check(TokenType.FIJATE_SI)) {
            return parseIfStatement();
        }

        if (check(TokenType.SEGUN_VEA)) {
            return parseWhileStatement();
        }

        if (check(TokenType.SUELTA)) {
            ReturnNode returnStmt = parseReturnStatement();
            consume(TokenType.SEMICOLON, "Se esperaba ';' después de la sentencia return.");
            return returnStmt;
        }

        if (check(TokenType.DI)) {
            PrintNode printStmt = parsePrintStatement();
            consume(TokenType.SEMICOLON, "Se esperaba ';' después de la sentencia print.");
            return printStmt;
        }

        throw error(peek(), "Sentencia no válida.");
    }

    private DeclarationNode parseDeclaration() {
        boolean isConstant;

        if (match(TokenType.UN_TAL)) {
            isConstant = false;
        } else if (match(TokenType.A_MUERTE)) {
            isConstant = true;
        } else {
            throw error(peek(), "Se esperaba 'unTal' o 'aMuerte' al inicio de la declaración.");
        }

        String type = parseType();
        Token identifier = consume(TokenType.IDENTIFIER, "Se esperaba un identificador en la declaración.");

        ExpressionNode initializer = null;

        if (isConstant) {
            consume(TokenType.ASSIGN, "Una constante debe inicializarse con '='.");
            initializer = parseExpression();
        } else {
            if (match(TokenType.ASSIGN)) {
                initializer = parseExpression();
            }
        }

        return new DeclarationNode(isConstant, type, identifier.getLexeme(), initializer);
    }

    private AssignmentNode parseAssignment() {
        Token identifier = consume(TokenType.IDENTIFIER, "Se esperaba un identificador.");
        consume(TokenType.ASSIGN, "Se esperaba '=' en la asignación.");
        ExpressionNode value = parseExpression();

        return new AssignmentNode(identifier.getLexeme(), value);
    }

    private PrintNode parsePrintStatement() {
        consume(TokenType.DI, "Se esperaba 'di!'.");
        consume(TokenType.LPAREN, "Se esperaba '(' después de 'di!'.");

        List<ExpressionNode> args = new ArrayList<>();

        if (!check(TokenType.RPAREN)) {
            args.add(parseExpression());

            while (match(TokenType.COMMA)) {
                args.add(parseExpression());
            }
        }

        consume(TokenType.RPAREN, "Se esperaba ')' al final de la llamada a print.");
        return new PrintNode(args);
    }

    private IfNode parseIfStatement() {
        consume(TokenType.FIJATE_SI, "Se esperaba 'fijateSi'.");
        consume(TokenType.LPAREN, "Se esperaba '(' después de 'fijateSi'.");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RPAREN, "Se esperaba ')' después de la condición del if.");

        BlockNode thenBlock = parseBlock();
        BlockNode elseBlock = null;

        if (match(TokenType.YSINO)) {
            elseBlock = parseBlock();
        }

        return new IfNode(condition, thenBlock, elseBlock);
    }

    private WhileNode parseWhileStatement() {
        consume(TokenType.SEGUN_VEA, "Se esperaba 'segúnVea'.");
        consume(TokenType.LPAREN, "Se esperaba '(' después de 'segúnVea'.");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RPAREN, "Se esperaba ')' después de la condición del while.");

        BlockNode body = parseBlock();
        return new WhileNode(condition, body);
    }

    private FunctionDeclNode parseFunctionDecl() {
        consume(TokenType.LA_FAENA, "Se esperaba 'laFaena'.");

        String returnType;
        if (match(TokenType.NA_DE_NA)) {
            returnType = "naDeNa";
        } else {
            returnType = parseType();
        }

        Token name = consume(TokenType.IDENTIFIER, "Se esperaba el nombre de la función.");
        consume(TokenType.LPAREN, "Se esperaba '(' después del nombre de la función.");

        List<ParameterNode> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            parameters = parseParameterList();
        }

        consume(TokenType.RPAREN, "Se esperaba ')' después de los parámetros.");
        BlockNode body = parseBlock();

        return new FunctionDeclNode(returnType, name.getLexeme(), parameters, body);
    }

    private List<ParameterNode> parseParameterList() {
        List<ParameterNode> parameters = new ArrayList<>();

        parameters.add(parseParameter());
        while (match(TokenType.COMMA)) {
            parameters.add(parseParameter());
        }

        return parameters;
    }

    private ParameterNode parseParameter() {
        String type = parseType();
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba un identificador en el parámetro.");
        return new ParameterNode(type, name.getLexeme());
    }

    private ReturnNode parseReturnStatement() {
        consume(TokenType.SUELTA, "Se esperaba 'suelta'.");

        ExpressionNode value = null;

        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }

        return new ReturnNode(value);
    }

    private String parseType() {
        if (match(TokenType.PAVOS)) {
            return "pavos";
        }
        if (match(TokenType.APACHAS)) {
            return "aPachas";
        }
        if (match(TokenType.CARRO)) {
            return "carro";
        }
        if (match(TokenType.JURA)) {
            return "jura";
        }

        throw error(peek(), "Se esperaba un tipo válido: pavos, aPachas, carro o jura.");
    }

    // =========================
    // EXPRESSIONS WITH PRECEDENCE
    // =========================

    private ExpressionNode parseExpression() {
        return parseOr();
    }

    private ExpressionNode parseOr() {
        ExpressionNode expr = parseAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            ExpressionNode right = parseAnd();
            expr = new BinaryExprNode(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private ExpressionNode parseAnd() {
        ExpressionNode expr = parseComparison();

        while (match(TokenType.AND)) {
            Token operator = previous();
            ExpressionNode right = parseComparison();
            expr = new BinaryExprNode(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private ExpressionNode parseComparison() {
        ExpressionNode expr = parseAddition();

        while (match(TokenType.EQUAL_EQUAL, TokenType.LESS, TokenType.GREATER,
                TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = parseAddition();
            expr = new BinaryExprNode(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private ExpressionNode parseAddition() {
        ExpressionNode expr = parseMultiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode right = parseMultiplication();
            expr = new BinaryExprNode(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private ExpressionNode parseMultiplication() {
        ExpressionNode expr = parseUnary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            ExpressionNode right = parseUnary();
            expr = new BinaryExprNode(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private ExpressionNode parseUnary() {
        if (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode right = parseUnary();
            return new UnaryExprNode(operator.getLexeme(), right);
        }

        return parsePrimary();
    }

    private ExpressionNode parsePrimary() {
        if (match(TokenType.INT_LITERAL)) {
            return new LiteralExprNode(previous().getLexeme());
        }

        if (match(TokenType.FLOAT_LITERAL)) {
            return new LiteralExprNode(previous().getLexeme());
        }

        if (match(TokenType.STRING_LITERAL)) {
            return new LiteralExprNode(previous().getLexeme());
        }

        if (match(TokenType.JURAO)) {
            return new LiteralExprNode("jurao");
        }

        if (match(TokenType.BULO)) {
            return new LiteralExprNode("bulo");
        }

        if (match(TokenType.IDENTIFIER)) {
            return new IdentifierExprNode(previous().getLexeme());
        }

        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = parseExpression();
            consume(TokenType.RPAREN, "Se esperaba ')' después de la expresión.");
            return expr;
        }

        throw error(peek(), "Se esperaba un literal, un identificador o una expresión entre paréntesis.");
    }

    // =========================
    // TOKEN UTILS
    // =========================

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
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return type == TokenType.EOF;
        }
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
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

    private ParseException error(Token token, String message) {
        return new ParseException(
                "Error sintáctico en línea " + token.getLine() +
                        ", columna " + token.getColumn() +
                        ": " + message + " Token encontrado: '" + token.getLexeme() + "'"
        );
    }
}