package lexer;

public class Token {
    /*
    Assumeixo que tens o tindràs aquestes coses:
        Token
            TokenType getType()
            String getLexeme()
            int getLine()
            int getColumn()

        TokenType amb noms similars a:
            EOF
            UN_TAL, A_MUERTE
            FIJATE_SI, YSINO, SEGUN_VEA
            LA_FAENA, SUELTA, DI
            NA_DE_NA
            PAVOS, APACHAS, CARRO, JURA
            JURAO, BULO
            IDENTIFIER
            INT_LITERAL, FLOAT_LITERAL, STRING_LITERAL
            LPAREN, RPAREN, LBRACE, RBRACE
            SEMICOLON, COMMA, ASSIGN
            PLUS, MINUS, STAR, SLASH
            OR, AND
            EQUAL_EQUAL, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL

        AST base
            ProgramNode
            GlobalElementNode
            StatementNode
            BlockNode
            DeclarationNode
            AssignmentNode
            PrintNode
            IfNode
            WhileNode
            FunctionDeclNode
            ReturnNode
            ParameterNode
            ExpressionNode

        subclasses d’expressió com:
            BinaryExprNode
            UnaryExprNode
            LiteralExprNode
            IdentifierExprNode
    */

}
