package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
	private static final Map<String, TokenType> KEYWORDS = buildKeywords();

	private final String source;
	private final List<Token> tokens;

	private int start;
	private int current;
	private int line;
	private int column;

	private int tokenLine;
	private int tokenColumn;

	public Lexer(String source) {
		this.source = source == null ? "" : source;
		this.tokens = new ArrayList<>();
		this.start = 0;
		this.current = 0;
		this.line = 1;
		this.column = 1;
		this.tokenLine = 1;
		this.tokenColumn = 1;
	}

	public List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			tokenLine = line;
			tokenColumn = column;
			scanToken();
		}

		tokens.add(new Token(TokenType.EOF, "", line, column));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			case ' ':
			case '\r':
			case '\t':
			case '\n':
				break;
			case '{':
				addToken(TokenType.LBRACE);
				break;
			case '}':
				addToken(TokenType.RBRACE);
				break;
			case '(':
				addToken(TokenType.LPAREN);
				break;
			case ')':
				addToken(TokenType.RPAREN);
				break;
			case ',':
				addToken(TokenType.COMMA);
				break;
			case ';':
				addToken(TokenType.SEMICOLON);
				break;
			case '+':
				addToken(TokenType.PLUS);
				break;
			case '-':
				addToken(TokenType.MINUS);
				break;
			case '*':
				addToken(TokenType.STAR);
				break;
			case '=':
				addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
				break;
			case '<':
				addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
				break;
			case '>':
				addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
				break;
			case '/':
				if (match('/')) {
					skipLineComment();
				} else if (match('*')) {
					skipBlockComment();
				} else {
					addToken(TokenType.SLASH);
				}
				break;
			case '"':
				string();
				break;
			case '\'':
				character();
				break;
			default:
				if (isDigit(c)) {
					number();
				} else if (isIdentifierStart(c)) {
					identifier();
				} else {
					throw error("Caracter no reconocido: '" + c + "'");
				}
				break;
		}
	}

	private void skipLineComment() {
		while (!isAtEnd() && peek() != '\n') {
			advance();
		}
	}

	private void skipBlockComment() {
		while (!isAtEnd()) {
			if (peek() == '*' && peekNext() == '/') {
				advance();
				advance();
				return;
			}
			advance();
		}

		throw error("Comentario de bloque sin cerrar.");
	}

	private void string() {
		while (!isAtEnd() && peek() != '"') {
			advance();
		}

		if (isAtEnd()) {
			throw error("Cadena de texto sin cerrar.");
		}

		advance(); // Consumir comilla de cierre.
		String value = source.substring(start + 1, current - 1);
		addToken(TokenType.STRING_LITERAL, value);
	}

	private void character() {
		if (isAtEnd() || peek() == '\n') {
			throw error("Literal de caracter sin cerrar.");
		}

		char value;
		if (peek() == '\\') {
			advance();
			if (isAtEnd()) {
				throw error("Secuencia de escape incompleta en literal de caracter.");
			}
			value = readEscapedChar(advance());
		} else {
			value = advance();
		}

		if (!match('\'')) {
			throw error("Literal de caracter invalido. Debe contener un solo caracter.");
		}

		addToken(TokenType.CHAR_LITERAL, String.valueOf(value));
	}

	private char readEscapedChar(char escapeCode) {
		switch (escapeCode) {
			case 'n':
				return '\n';
			case 't':
				return '\t';
			case 'r':
				return '\r';
			case '\'':
				return '\'';
			case '"':
				return '"';
			case '\\':
				return '\\';
			default:
				throw error("Secuencia de escape no valida: \\" + escapeCode);
		}
	}

	private void number() {
		while (isDigit(peek())) {
			advance();
		}

		boolean isFloat = false;
		if (peek() == '.' && isDigit(peekNext())) {
			isFloat = true;
			advance(); // Consumir el punto.
			while (isDigit(peek())) {
				advance();
			}
		}

		String lexeme = source.substring(start, current);
		addToken(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL, lexeme);
	}

	private void identifier() {
		while (isIdentifierPart(peek())) {
			advance();
		}

		String lexeme = source.substring(start, current);

		// Soporta la forma "di!" sin crear token separado para '!'.
		if ("di".equals(lexeme) && match('!')) {
			addToken(TokenType.DI, "di!");
			return;
		}

		TokenType keywordType = KEYWORDS.get(lexeme);
		if (keywordType != null) {
			addToken(keywordType, lexeme);
			return;
		}

		addToken(TokenType.IDENTIFIER, lexeme);
	}

	private void addToken(TokenType type) {
		addToken(type, source.substring(start, current));
	}

	private void addToken(TokenType type, String lexeme) {
		tokens.add(new Token(type, lexeme, tokenLine, tokenColumn));
	}

	private char advance() {
		char c = source.charAt(current);
		current++;

		if (c == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}

		return c;
	}

	private boolean match(char expected) {
		if (isAtEnd()) {
			return false;
		}

		if (source.charAt(current) != expected) {
			return false;
		}

		advance();
		return true;
	}

	private char peek() {
		if (isAtEnd()) {
			return '\0';
		}
		return source.charAt(current);
	}

	private char peekNext() {
		if (current + 1 >= source.length()) {
			return '\0';
		}
		return source.charAt(current + 1);
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isIdentifierStart(char c) {
		return Character.isLetter(c) || c == '_';
	}

	private boolean isIdentifierPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	private LexicalException error(String message) {
		return new LexicalException(message, tokenLine, tokenColumn);
	}

	private static Map<String, TokenType> buildKeywords() {
		Map<String, TokenType> keywords = new HashMap<>();

		keywords.put("unTal", TokenType.UN_TAL);
		keywords.put("aMuerte", TokenType.A_MUERTE);
		keywords.put("fijateSi", TokenType.FIJATE_SI);
		keywords.put("ySiNo", TokenType.Y_SI_NO);
		keywords.put("segunVea", TokenType.SEGUN_VEA);
		keywords.put("segúnVea", TokenType.SEGUN_VEA);
		keywords.put("laFaena", TokenType.LA_FAENA);
		keywords.put("suelta", TokenType.SUELTA);
		keywords.put("di", TokenType.DI);
		keywords.put("naDeNa", TokenType.NA_DE_NA);
		keywords.put("pavos", TokenType.PAVOS);
		keywords.put("aPachas", TokenType.APACHAS);
		keywords.put("carro", TokenType.CARRO);
		keywords.put("letrilla", TokenType.LETRILLA);
		keywords.put("jura", TokenType.JURA);
		keywords.put("jurao", TokenType.JURAO);
		keywords.put("bulo", TokenType.BULO);

		keywords.put("y", TokenType.AND);
		keywords.put("o", TokenType.OR);

		return keywords;
	}
}
