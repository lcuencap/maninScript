import ast.ASTVisualizer;
import ast.ProgramNode;
import lexer.Lexer;
import lexer.LexicalException;
import lexer.Token;
import parser.Parser;
import parser.ParserException;
import semantic.SemanticAnalyzer;
import semantic.SemanticException;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = """
                unTal pavos edad = 21;
                aMuerte jura esMayor = jurao;
                laFaena pavos suma(pavos a, pavos b) {
                    suelta a + b;
                }
                laFaena naDeNa saluda(letrilla nombre) {
                    fijateSi (edad >= 18 y esMayor == jurao) {
                        di!("Hola", nombre, suma(2, 3));
                    } ySiNo {
                        di!("No puedes pasar");
                    }
                }
                """;

        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parseProgram();

            SemanticAnalyzer semantic = new SemanticAnalyzer();
            semantic.analyze(program);

            ASTVisualizer.show(program, source);
        } catch (LexicalException | ParserException | SemanticException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
