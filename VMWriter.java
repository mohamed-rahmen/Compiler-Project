import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that write all the possible VM commands.
 */
public class VMWriter {

    /**
     * Function write push command.
     * @param vmFile file to write in.
     * @param segment to push from.
     * @param index in which index in the segment.
     */
    static void writePush(FileWriter vmFile, String segment, int index) throws IOException {
        if (segment.equals("field")) segment = "this";
        vmFile.write("push " + segment + " " + index + "\n");
    }

    /**
     * Function write pop command.
     * @param vmFile file to write in.
     * @param segment to pop for.
     * @param index in which index in the segment.
     */
    static void writePop(FileWriter vmFile, String segment, int index) throws IOException {
        if (segment.equals("field")) segment = "this";
        vmFile.write("pop " + segment + " " + index + "\n");
    }

    /**
     * Function write the command in the vm file.
     * @param vmFile to write in.
     * @param command to write.
     */
    static void writeArithmetic(FileWriter vmFile, String command) throws IOException {
        vmFile.write(command + "\n");
    }

    /**
     * Function write the label command in the vm file.
     * @param vmFile to write in.
     * @param label to write.
     */
    static void writeLabel(FileWriter vmFile, String label) throws IOException {
        vmFile.write("label " + label + "\n");
    }

    /**
     * Function write the goto command in the vm file.
     * @param vmFile to write in.
     * @param label to write.
     */
    static void writeGoto(FileWriter vmFile, String label) throws IOException {
        vmFile.write("goto " + label + "\n");
    }

    /**
     * Function write the if-goto command in the vm file.
     * @param vmFile to write in.
     * @param label to write.
     */
    static void writeIf(FileWriter vmFile, String label) throws IOException {
        vmFile.write("if-goto " + label + "\n");
    }

    /**
     * Function write the call command in the vm file.
     * @param vmFile to write in.
     * @param name to write.
     * @param nLocals number of the local to pass to the called function.
     */
    static void writeCall(FileWriter vmFile, String name, int nLocals) throws IOException {
        vmFile.write("call " + name + " " + nLocals + "\n");
    }

    /**
     * Function write the function command in the vm file.
     * @param vmFile to write in.
     * @param name to write.
     * @param nLocals number of the arguments that the function get.
     */
    static void writeFunction(FileWriter vmFile, String name, int nLocals) throws IOException {
        vmFile.write("function " + name + " " + nLocals + "\n");
    }

    /**
     * Function write the return command in the vm file.
     * @param vmFile to write in.
     */
    static void writeReturn(FileWriter vmFile) throws IOException {
        vmFile.write("return\n");
    }

}
