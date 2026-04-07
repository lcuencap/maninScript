import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.ParserException;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // === FASE 1: prueba temporal del nuevo sistema de errores del parser ===
        // Estos casos se eliminarán en la Fase 4, cuando Main pase a parsear el
        // ejemplo Fibonacci de verdad. De momento solo validan que ParserException
        // construye mensajes en español con línea/columna correctas.

        String[] casosError = {
                // Falta ';' tras una declaración global mutable.
                "unTal pavos edad = 21\n",
                // Constante 'aMuerte' sin inicializador.
                "aMuerte pavos MAX;\n",
                // Falta ')' cerrando la condición de un fijateSi dentro de una función.
                "laFaena naDeNa f() { fijateSi (1 == 1 { di!(\"hola\"); } }\n",
                // Tipo inválido en parámetro de función.
                "laFaena pavos suma(foo n) { suelta n; }\n",
                // Llamada a función mal cerrada.
                "laFaena pavos f() { suelta g(1, 2 ; }\n",
        };

        for (int i = 0; i < casosError.length; i++) {
            String fuente = casosError[i];
            System.out.println("--- Caso " + (i + 1) + " ---");
            System.out.println("Fuente: " + fuente.replace("\n", "\\n"));
            try {
                Lexer lexer = new Lexer(fuente);
                List<Token> tokens = lexer.scanTokens();
                Parser parser = new Parser(tokens);
                parser.parseProgram();
                System.out.println("INESPERADO: el parser no lanzó error.");
            } catch (ParserException ex) {
                System.out.println("OK ParserException:");
                System.out.println("  mensaje  : " + ex.getMessage());
                System.out.println("  línea    : " + ex.getLine());
                System.out.println("  columna  : " + ex.getColumn());
                System.out.println("  esperado : " + ex.getExpected());
                System.out.println("  encontrado: '" + ex.getFoundLexeme() + "'");
            }
            System.out.println();
        }

        // Caso positivo: el snippet original tiene que parsear sin errores.
        String fuenteOk = """
                aMuerte jura esMayor = jurao;
                laFaena naDeNa saluda(letrilla nombre) {
                    fijateSi (esMayor == jurao) {
                        di!("Hola", nombre);
                    } ySiNo {
                        di!("No puedes pasar");
                    }
                }
                """;
        System.out.println("--- Caso positivo ---");
        try {
            Lexer lexer = new Lexer(fuenteOk);
            List<Token> tokens = lexer.scanTokens();
            Parser parser = new Parser(tokens);
            parser.parseProgram();
            System.out.println("OK: el parser aceptó el programa válido.");
        } catch (ParserException ex) {
            System.out.println("INESPERADO: " + ex.getMessage());
        }
    }
}
