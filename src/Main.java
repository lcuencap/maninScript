import ast.ASTPrinter;
import ast.ProgramNode;
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

        // === Casos positivos: cubren los puntos delicados de la BNF ===
        String[] casosOk = {
                // Original: declaración global, función, if/else, di! con varios args.
                """
                aMuerte jura esMayor = jurao;
                laFaena naDeNa saluda(letrilla nombre) {
                    fijateSi (esMayor == jurao) {
                        di!("Hola", nombre);
                    } ySiNo {
                        di!("No puedes pasar");
                    }
                }
                """,
                // Precedencia de expresiones: comprobación a ojo del árbol esperado en Fase 3.
                // La gramática debe interpretar a + b * -c == d y e o f como
                //   ((a + (b * (-c))) == d) y e) o f
                """
                laFaena pavos calcula(pavos a, pavos b, pavos c, pavos d, jura e, jura f) {
                    suelta a + b * -c;
                }
                """,
                // if sin else (else_opcional → ε), while con tilde, return sin valor.
                """
                laFaena naDeNa bucle(pavos n) {
                    segúnVea (n > 0) {
                        di!(n);
                        n = n - 1;
                    }
                    fijateSi (n == 0) {
                        suelta;
                    }
                }
                """,
                // Función sin parámetros (parametros_opc → ε) y di! sin argumentos
                // (args_print_opc → ε). Llamada a función como expresión, anidada.
                """
                laFaena pavos cero() { suelta 0; }
                laFaena pavos suma(pavos a, pavos b) { suelta a + b; }
                laFaena naDeNa main() {
                    di!();
                    di!(suma(cero(), suma(1, 2)));
                }
                """,
                // Constante de cada tipo primitivo + literales bool/char/string/float.
                """
                aMuerte pavos    A = 10;
                aMuerte aPachas  B = 3.14;
                aMuerte carro    C = 'x';
                aMuerte letrilla D = "hola";
                aMuerte jura     E = bulo;
                """,
        };

        for (int i = 0; i < casosOk.length; i++) {
            System.out.println("--- Caso positivo " + (i + 1) + " ---");
            try {
                Lexer lexer = new Lexer(casosOk[i]);
                List<Token> tokens = lexer.scanTokens();
                Parser parser = new Parser(tokens);
                parser.parseProgram();
                System.out.println("OK: aceptado.");
            } catch (ParserException ex) {
                System.out.println("INESPERADO: " + ex.getMessage());
            }
        }

        // === FASE 3: verificación visual del AST con ASTPrinter ===

        // Precedencia: esperamos el árbol
        //   Binary ==
        //     Binary +
        //       Identifier a
        //       Binary *
        //         Identifier b
        //         Unary -
        //           Identifier c
        //     Identifier d
        String fuentePrecedencia = """
                laFaena pavos prec(pavos a, pavos b, pavos c, pavos d) {
                    suelta a + b * -c == d;
                }
                """;
        System.out.println();
        System.out.println("--- AST de precedencia: a + b * -c == d ---");
        System.out.println(parseYImprimir(fuentePrecedencia));

        // Fibonacci: el objetivo explícito de la sección 5 del PLAN.
        String fuenteFib = """
                laFaena pavos fibonacci(pavos n) {
                    fijateSi (n <= 1) {
                        suelta n;
                    }
                    suelta fibonacci(n - 1) + fibonacci(n - 2);
                }
                """;
        System.out.println("--- AST de Fibonacci ---");
        System.out.println(parseYImprimir(fuenteFib));
    }

    private static String parseYImprimir(String fuente) {
        try {
            Lexer lexer = new Lexer(fuente);
            List<Token> tokens = lexer.scanTokens();
            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parseProgram();
            return ASTPrinter.print(program);
        } catch (ParserException ex) {
            return "ERROR: " + ex.getMessage();
        }
    }
}
