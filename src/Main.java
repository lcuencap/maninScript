import ast.ASTPrinter;
import ast.ProgramNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = """
                unTal pavos edad = 'a';
                unTal pavos edad = 21;
                aMuerte jura esMayor = jurao;
                laFaena naDeNa saluda(letrilla nombre) {
                    fijateSi (edad >= 18 y esMayor == jurao) {
                        di!("Hola", nombre);
                    } ySiNo {
                        di!("No puedes pasar");
                    }
                }
                """;

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }

        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parseProgram();
        System.out.print(ASTPrinter.print(program));
    }
}
