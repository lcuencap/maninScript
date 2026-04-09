package ast.expressions;

import lexer.TokenType;

public class LiteralExprNode extends ExpressionNode {
    private final TokenType literalType;
    private final String value;

    public LiteralExprNode(TokenType literalType, String value) {
        this.literalType = literalType;
        this.value = value;
    }

    public TokenType getLiteralType() { return literalType; }
    public String getValue() { return value; }
}
