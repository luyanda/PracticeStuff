import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class Mutator {

    static CompilationUnit cu;
    static BufferedWriter writer;
    static String filename;
    static int counter;

    public static void main(String[] args) throws IOException {
        // Create an input stream for the file to be parsed
        FileInputStream sourceFileInputStream = new FileInputStream(args[0]);

        // Store class name
        filename = args[0].split(Pattern.quote("."))[0];

        // Initialize the counter
        counter = 0;

        // Parse the file
        cu = JavaParser.parse(sourceFileInputStream);


        // Create a new instance of our mutator and feed it the AST
        ModifierVisitor<?> conditionalMutator = new ConditionalMutator();
        conditionalMutator.visit(cu, null);


    }

    private static class ConditionalMutator extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(BinaryExpr n, Void arg) {
            if (isRelationalOperator(n.getOperator())) {
                String line = n.getBegin().get().toString();
                String operator = n.getOperator().toString();
                System.out.println(String.format("Found relational operator %s at %s", n.getOperator().toString(), n.getBegin().get().toString()));

                writeFile(operator, line, n);

                n.setOperator(BinaryExpr.Operator.valueOf(operator));

            }


            return super.visit(n, arg);
        }
    }

    /**
     * Check if a given {@link BinaryExpr.Operator} is relational.
     *
     * @param op The {@link BinaryExpr.Operator} to be tested.
     * @return true if the operator is relation, false otherwise
     */
    private static boolean isRelationalOperator(BinaryExpr.Operator op) {
        switch (op) {
            case LESS:
            case LESS_EQUALS:
            case GREATER:
            case GREATER_EQUALS:
            case NOT_EQUALS:
            case EQUALS:
                return true;
            default:
                return false;
        }
    }

    /**
     * coping mutants to disk
     *
     * @param op The {@link BinaryExpr.Operator} found and to be replaced
     * @param l  The line number where the relap was found
     * @param n
     */
    private static void writeFile(String op, String l, BinaryExpr n) {
        String[] s = {"LESS_EQUALS", "EQUALS", "GREATER", "GREATER_EQUALS", "LESS", "NOT_EQUALS"};

        for (int i = 0; i < s.length; i++) {
            if (s[i] != op) {
                n.setOperator(BinaryExpr.Operator.valueOf(s[i]));
                try {
                    writer = new BufferedWriter(new FileWriter(filename + counter + ".java"));
                    writer.write(cu.toString());
                    counter += 1;

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}


