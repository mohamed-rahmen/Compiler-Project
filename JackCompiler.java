import java.io.*;

/**
 * The main class that manages tokenize and compiling the files.
 */
public class JackCompiler {

    public static void main(String[] args) {
        //Input file
        File inputFile = new File(args[0]);
        File outputFile;//will hold the outfile matching each input file.
        File[] files;//will hold all input files.
        if (inputFile.isDirectory()) {
            files = inputFile.listFiles();
        } else {
            files = new File[]{inputFile};
        }

        for (File f : files) {
            if (!f.isDirectory() && f.getName().endsWith(".jack")) {
                String outFileName = f.getName().split("\\.")[0] + ".vm";
                outputFile = new File(f.getParent(), outFileName);
                try
                    (RandomAccessFile jackFile = new RandomAccessFile(f, "r");
                    FileWriter xmlFile = new FileWriter(outputFile))
                {
                    JackTokenizer jackTokenizer = new JackTokenizer(jackFile);
                    while (jackTokenizer.hasMoreTokens()) {
                        try {
                            CompilationEngineVM.compileClass(jackTokenizer, xmlFile);
                        }catch (Exception e){
                            xmlFile.close();
                            jackFile.close();
                            System.err.println(e.getMessage());
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
            }
        }
    }
}
