import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DynamicCompiler<T> {

    private JavaCompiler compiler;
    private FileManager fileManager;
    private DynamicClassLoader classLoader;

    private DiagnosticCollector<JavaFileObject> diagnostics;

    public DynamicCompiler() throws DynamicCompilerException {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new DynamicCompilerException("Compiler not found");
        }

        classLoader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
        diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager = new FileManager(standardFileManager, classLoader);
    }

    public synchronized Class<T> compile(String className, String javaSource) throws DynamicCompilerException {
        return compile(null, className, javaSource);
    }

    @SuppressWarnings("unchecked")
    public synchronized Class<T> compile(String packageName, String className, String javaSource) throws DynamicCompilerException {
        try {
            Path temp = writeSourceAndClass(className, javaSource);
            String qualifiedClassName = DynamicCompilerUtils.getQualifiedClassName(packageName, className);
            DynamicStringObject sourceObj = new DynamicStringObject(className, javaSource);

            final String relativeName = className + JavaFileObject.Kind.SOURCE.extension;
            fileManager.putFileForInput(packageName, relativeName, sourceObj);

            List<String> optionList = new ArrayList<>();
            optionList.add("-classpath");
            optionList.add(System.getProperty("java.class.path") + System.getProperty("path.separator") + temp.toString());
            CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, Collections.singletonList(sourceObj));
            boolean result = task.call();

            if (!result) {
                throw new DynamicCompilerException("Compilation failure", diagnostics.getDiagnostics());
            }

            fileManager.close();
            return (Class<T>) classLoader.loadClass(qualifiedClassName);
        } catch (Exception exception) {
            throw new DynamicCompilerException(exception, diagnostics.getDiagnostics());
        }
    }

    private Path writeSourceAndClass(String className, String javaSource) throws IOException {
        Path temp = Paths.get(System.getProperty("java.io.tmpdir"), "__CLASSPATH__");
        Files.createDirectories(temp);
        Path javaSourceFile = Paths.get(temp.normalize().toAbsolutePath().toString(), className + ".java");
        System.out.println("The java source file is located at " + javaSourceFile);

        Files.write(javaSourceFile, javaSource.getBytes());
        return temp;
    }
}
