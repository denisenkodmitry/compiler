import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Compiler {

    public static void main(String[] args) {
        try {
            String className = "TestClass1";
            String code = "public class " + className + " {\n" +

                    "public static int run() {\n" +
                    "       return 5; \n" +
                    "    }" +
                    "}";

            final DynamicCompiler compiler = new DynamicCompiler();
            Class<?> compile = compiler.compile(className, code);
            System.out.println(compile);
            className = "TestClass2";
            code = "public class " + className + " {\n" +

                    "public static int run() {\n" +
                    "       System.out.println(TestClass1.run()); return 3; \n" +
                    "    }" +
                    "}";

            compile = compiler.compile( className, code);

            System.out.println(compile);

            className = "TestClass3";
            code = "import org.junit.jupiter.api.Test;\n" +
                    "import static org.junit.jupiter.api.Assertions.assertEquals;\n"+
                    "public class " + className + " {\n" +

                    "    @Test\n" +
                    "    void exceptionTesting1() {\n" +
                    "        assertEquals(TestClass2.run(), 2, \"test\");\n" +
                    "        assertEquals(\"1\", \"7\", \"test\");\n" +
                    "    }\n" +
                    "\n" +
                    "    @Test\n" +
                    "    void exceptionTesting2() {\n" +
                    "        assertEquals(\"1\", \"1\", \"test\");\n" +
                    "        assertEquals(\"1\", \"8\", \"test\");\n" +
                    "    }"
                    +"}";
            compile = compiler.compile( className, code);
            JUnitStarter.run(compile);

        } catch (DynamicCompilerException e) {
            System.out.println(e.getDiagnosticsError());
        }

    }
}
