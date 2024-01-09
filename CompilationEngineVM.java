import java.io.FileWriter;
import java.io.IOException;

class CompilationEngineVM {

    /**
     * Jack tokenizer.
     */
    private static JackTokenizer jackTokenizer = null;

    private static int labelIndex = 0;

    /**
     * Function try to compile a symbol to the xml file, check if this symbol equals the current token.
     *
     * @param symbol to write.
     * @param vmFile file to write in.
     * @throws Exception
     */
    private static void compileSymbol(String symbol, FileWriter vmFile) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo(symbol)) {
                return;
            } else {
                throw new Exception("Not the fit symbol. '" + symbol + "' expected. Got " + jackTokenizer.currentToken);
            }
        } else {
            throw new Exception("Expected " + symbol + "symbol here.");
        }
    }

    private static void compileSpecialSymbols(String symbol, FileWriter vmFile) throws Exception {
        switch (symbol) {
            case "&":
                break;
            case "<":
                break;
            case ">":
                break;
            default:
                jackTokenizer.preAdvance();
                compileSymbol(symbol, vmFile);
        }
    }

    /**
     * Function try to compile a keyword line to the xml file.
     *
     * @param vmFile file to write in.
     */
    private static void compileKeyWord(FileWriter vmFile) throws IOException {

    }

    /**
     * Function try to compile identifier to the xml file.
     *
     * @param vmFile output file.
     * @throws Exception
     */
    private static void compileIdentifier(FileWriter vmFile) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if ((jackTokenizer.tokenType()).equals("identifier")) {
                return;
            } else throw new Exception("Not Identifier token must be identifier.");
        } else throw new Exception("Identifier expected.");
    }

    /**
     * Function compile a valid type to xml file.
     *
     * @param vmFile to write in.
     * @throws Exception
     */
    private static void compileType(FileWriter vmFile, boolean subroutineCase) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo("int") ||
                    jackTokenizer.equalTo("char") ||
                    jackTokenizer.equalTo("boolean") ||
                    (subroutineCase && jackTokenizer.equalTo("void"))) {
                return;
            } else if ((jackTokenizer.tokenType()).equals("identifier")) {
                return;
            } else
                throw new Exception("Invalid type: int | char | boolean | className or void in subroutine case.");
        }
    }

    private static void writePushOrPopVar
            (FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable, String name, boolean popCase) throws IOException{
        int index = subroutineTable.indexOf(name);
        String kind = subroutineTable.kindOf(name);
        if (index == SymbolTable.NOT_FOUND) {
            index = classTable.indexOf(name);
            kind = classTable.kindOf(name);
        }
        if (popCase) VMWriter.writePop(vmFile, kind, index);
        else VMWriter.writePush(vmFile, kind, index);
    }

    /**
     * Function compile a line that like: type varName (','varName)*
     *
     * @param vmFile output file.
     * @throws Exception
     */
    private static void compileMoreVarName
    (FileWriter vmFile, boolean parameterList, String type, String kind, SymbolTable table) throws Exception {
        boolean moreVarName = true;
        while (moreVarName) {
            if (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                if (jackTokenizer.equalTo(",")) {
                    jackTokenizer.preAdvance();
                    compileSymbol(",", vmFile);
                    if (parameterList) {
                        compileType(vmFile, false);
                        type = jackTokenizer.currentToken;
                    }
                    compileIdentifier(vmFile);
                    table.define(jackTokenizer.currentToken, type, kind);
                } else {
                    moreVarName = false;
                    jackTokenizer.preAdvance();
                }
            } else {
                moreVarName = false;
            }
        }
    }

    private static void compileSubroutineCall(FileWriter vmFile, String name, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            String className = classTable.name;
            int nLocals = 0;
            boolean classOrVarNameCase = false;
            boolean printLocal = false;
            boolean printPointer = false;
            boolean printThis = false;
            if (jackTokenizer.equalTo(".")) {
                if (!(subroutineTable.indexOf(name) == -1)){
                    name = subroutineTable.typeOf(name);
                    nLocals++;
                    printLocal = true;
                }
                else if (!(classTable.indexOf(name) == -1)){
                    name = classTable.typeOf(name);
                    nLocals++;
                    printThis = true;
                }
                jackTokenizer.preAdvance();
                compileSymbol(".", vmFile);
                compileIdentifier(vmFile);
                name += "." + jackTokenizer.currentToken;
                classOrVarNameCase = true;
            }
            if (!classOrVarNameCase) {
                jackTokenizer.preAdvance();
                name = className + "." + name;
                nLocals++;
                printPointer = true;
            }
            compileSymbol("(", vmFile);
            if (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                if (jackTokenizer.equalTo(")")) {
                    jackTokenizer.preAdvance();
                } else {
                    jackTokenizer.preAdvance();
                    nLocals += compileExpressionList(vmFile, classTable, subroutineTable);
                }
            }
            if (printLocal) VMWriter.writePush(vmFile, "local", 0);
            if (printPointer) VMWriter.writePush(vmFile, "pointer", 0);
            if (printThis) VMWriter.writePush(vmFile, "this", 0);


            VMWriter.writeCall(vmFile, name, nLocals);
            compileSymbol(")", vmFile);

        } else throw new Exception("Expected '.' symbol or '(' symbol.");
    }

    private static boolean compileClassVarDec(FileWriter vmFile, SymbolTable classSymbols) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();

            if (jackTokenizer.equalTo("static") || jackTokenizer.equalTo("field")) {

                String kind = jackTokenizer.currentToken;
                compileType(vmFile, false); //type
                String type = jackTokenizer.currentToken;

                compileIdentifier(vmFile); //varName
                classSymbols.define(jackTokenizer.currentToken, type, kind); // add the symbol to the table.
                compileMoreVarName(vmFile, false, type, kind, classSymbols); //(','varName)*
                compileSymbol(";", vmFile);

                return true;
            } else {
                jackTokenizer.preAdvance();
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean compileSubroutine
            (FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable, String className)
            throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo("constructor") || jackTokenizer.equalTo("function")
                    || jackTokenizer.equalTo("method")) {

                String define = jackTokenizer.currentToken;
                if (define.equals("method")) {
                    subroutineTable.define("this", className, SymbolTable.ARG);
                }

                compileType(vmFile, true);
                String functionType = jackTokenizer.currentToken; //save the function type to return correctly.

                compileIdentifier(vmFile); //subroutineName
                String functionName = className + "." + jackTokenizer.currentToken; //save function name.

                compileSymbol("(", vmFile);
                compileParameterList(vmFile, subroutineTable);
                compileSymbol(")", vmFile);

                compileSymbol("{", vmFile);
                while (compileVarDec(vmFile, subroutineTable)) ;

                //compileFunction.
                VMWriter.writeFunction(vmFile, functionName, subroutineTable.varCount(SymbolTable.VAR, false));
                if (define.equals("method")) {
                    VMWriter.writePush(vmFile, "argument", 0);
                    VMWriter.writePop(vmFile, "pointer", 0);
                } else if (define.equals("constructor")) {
                    int fieldCounter = classTable.varCount("field", false);
                    VMWriter.writePush(vmFile, "constant", fieldCounter);
                    VMWriter.writeCall(vmFile, "Memory.alloc", 1);
                    VMWriter.writePop(vmFile, "pointer", 0);
                }
                compileStatements(vmFile, functionType.equals("void"), classTable, subroutineTable);
                compileSymbol("}", vmFile);

                return true;
            } else {
                jackTokenizer.preAdvance();
                return false;
            }
        } else {
            return false;
        }
    }

    private static void compileParameterList(FileWriter vmFile, SymbolTable symbolTable) throws Exception {
        String type;
        try {
            compileType(vmFile, false); //type
            type = jackTokenizer.currentToken;
        } catch (Exception e) {
            jackTokenizer.preAdvance();
            return;
        }
        compileIdentifier(vmFile); //varName
        String name = jackTokenizer.currentToken;
        symbolTable.define(name, type, SymbolTable.ARG); //add the first argument if exist.

        compileMoreVarName(vmFile, true, "", SymbolTable.ARG, symbolTable); //(','type varName)*
    }

    private static boolean compileVarDec(FileWriter vmFile, SymbolTable symbolTable) throws Exception {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo("var")) {

                compileType(vmFile, false); //type
                String type = jackTokenizer.currentToken;

                compileIdentifier(vmFile); //varName
                String name = jackTokenizer.currentToken;

                symbolTable.define(name, type, SymbolTable.VAR);

                compileMoreVarName(vmFile, false, type, SymbolTable.VAR, symbolTable); //(','varName)*
                compileSymbol(";", vmFile);

                return true;
            } else {
                jackTokenizer.preAdvance();
                return false;
            }
        } else {
            return false;
        }
    }

    private static void compileStatements
            (FileWriter vmFile, boolean voidCase, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        boolean moreStatements = true;
        while (moreStatements) {
            if (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                switch (jackTokenizer.currentToken) {
                    case "let":
                        compileLet(vmFile, classTable, subroutineTable);
                        break;
                    case "if":
                        compileIf(vmFile, classTable, subroutineTable);
                        break;
                    case "while":
                        compileWhile(vmFile, classTable, subroutineTable);
                        break;
                    case "do":
                        compileDo(vmFile, classTable, subroutineTable);
                        break;
                    case "return":
                        compileReturn(vmFile, voidCase, classTable, subroutineTable);
                        break;
                    default:
                        moreStatements = false;
                        jackTokenizer.preAdvance();
                }
            } else {
                moreStatements = false;
            }
        }
    }

    private static void compileDo(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        compileIdentifier(vmFile);
        String name = jackTokenizer.currentToken;
        compileSubroutineCall(vmFile, name, classTable, subroutineTable);
        VMWriter.writePop(vmFile, "temp", 0);
        compileSymbol(";", vmFile);

    }

    private static void compileLet(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        compileIdentifier(vmFile);
        String name = jackTokenizer.currentToken;
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            boolean withExpressionCase = false; // Array
            if (jackTokenizer.equalTo("[")) {
                jackTokenizer.preAdvance();
                writePushOrPopVar(vmFile, classTable, subroutineTable, name, false);
                compileSymbol("[", vmFile);
                compileExpression(vmFile, classTable, subroutineTable);
                compileSymbol("]", vmFile);
                VMWriter.writeArithmetic(vmFile, "add");

                withExpressionCase = true;
            }
            if (!withExpressionCase) {
                jackTokenizer.preAdvance();
            }
            compileSymbol("=", vmFile);
            compileExpression(vmFile, classTable, subroutineTable);
            if (withExpressionCase) {
                VMWriter.writePop(vmFile, "temp", 0);
                VMWriter.writePop(vmFile, "pointer", 1);
                VMWriter.writePush(vmFile, "temp", 0);
                VMWriter.writePop(vmFile, "that", 0);
            }
            else {
                writePushOrPopVar(vmFile, classTable, subroutineTable, name, true);
            }
            compileSymbol(";", vmFile);
        } else throw new Exception("Must be '=' Symbol here...");
            }

    private static void compileWhile(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {
        String label1 = "L" + labelIndex++;
        String label2 = "L" + labelIndex++;

        VMWriter.writeLabel(vmFile, label1);
        compileSymbol("(", vmFile);
        compileExpression(vmFile, classTable, subroutineTable);
        compileSymbol(")", vmFile);
        VMWriter.writeArithmetic(vmFile, "not");
        VMWriter.writeIf(vmFile, label2);

        compileSymbol("{", vmFile);
        compileStatements(vmFile, false, classTable, subroutineTable);
        compileSymbol("}", vmFile);
        VMWriter.writeGoto(vmFile, label1);
        VMWriter.writeLabel(vmFile, label2);
    }

    private static void compileReturn(FileWriter vmFile, boolean voidCase, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo(";")) {
                jackTokenizer.preAdvance();
            } else {
                jackTokenizer.preAdvance();
                compileExpression(vmFile, classTable, subroutineTable);
            }
        }
        if (voidCase) VMWriter.writePush(vmFile, "constant", 0);
        VMWriter.writeReturn(vmFile);
        compileSymbol(";", vmFile);
    }

    private static void compileIf(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        compileSymbol("(", vmFile);
        compileExpression(vmFile, classTable, subroutineTable);
        VMWriter.writeArithmetic(vmFile, "not");
        String label1 = "L" + labelIndex++;
        VMWriter.writeIf(vmFile, label1);
        compileSymbol(")", vmFile);

        compileSymbol("{", vmFile);
        compileStatements(vmFile, false, classTable, subroutineTable);
        compileSymbol("}", vmFile);

        String label2 = "L" + labelIndex++;
        VMWriter.writeGoto(vmFile, label2);

        VMWriter.writeLabel(vmFile, label1);
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            if (jackTokenizer.equalTo("else")) {
                compileSymbol("{", vmFile);
                compileStatements(vmFile, false, classTable, subroutineTable);
                compileSymbol("}", vmFile);
            } else {
                jackTokenizer.preAdvance();
            }
        }

        VMWriter.writeLabel(vmFile, label2);
    }

    private static void compileExpression(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {
        boolean moreTerms = compileTerm(vmFile, classTable, subroutineTable);

        while (moreTerms) {
            if (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                if (jackTokenizer.equalTo("+") || jackTokenizer.equalTo("-") ||
                        jackTokenizer.equalTo("*") || jackTokenizer.equalTo("/") ||
                        jackTokenizer.equalTo("&") || jackTokenizer.equalTo("|") ||
                        jackTokenizer.equalTo("<") || jackTokenizer.equalTo(">") ||
                        jackTokenizer.equalTo("=")) {
                    String op = jackTokenizer.currentToken;
                    compileSpecialSymbols(jackTokenizer.currentToken, vmFile);
                    compileTerm(vmFile, classTable, subroutineTable);
                    pushOP(vmFile, op);
                } else {
                    moreTerms = false;
                    jackTokenizer.preAdvance();
                }
            } else moreTerms = false;
        }
    }

    private static void pushOP(FileWriter vmFile, String op) throws IOException {
        switch (op) {
            case "+":
                VMWriter.writeArithmetic(vmFile, "add");
                break;
            case "-":
                VMWriter.writeArithmetic(vmFile, "sub");
                break;
            case "*":
                VMWriter.writeCall(vmFile, "Math.multiply", 2);
                break;
            case "/":
                VMWriter.writeCall(vmFile, "Math.divide", 2);
                break;
            case "<":
                VMWriter.writeArithmetic(vmFile, "lt");
                break;
            case ">":
                VMWriter.writeArithmetic(vmFile, "gt");
                break;
            case "&":
                VMWriter.writeArithmetic(vmFile, "and");
                break;
            case "|":
                VMWriter.writeArithmetic(vmFile, "or");
                break;
            case "=":
                VMWriter.writeArithmetic(vmFile, "eq");
                break;
        }
    }

    private static boolean compileTerm
            (FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {

        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            String type = jackTokenizer.tokenType();

            if (type.equals("integerConstant")) {
                VMWriter.writePush(vmFile, "constant", Integer.parseInt(jackTokenizer.currentToken));
            }
            else if (type.equals("stringConstant")) {
                VMWriter.writePush(vmFile, "constant", (jackTokenizer.currentToken).length() - 1);
                VMWriter.writeCall(vmFile, "String.new", 1);
                for (int i = 1; i < jackTokenizer.currentToken.length(); i++) {
                    VMWriter.writePush(vmFile, "constant", (int) (jackTokenizer.currentToken).charAt(i));
                    VMWriter.writeCall(vmFile, "String.appendChar", 2);
                }

            }
            else if (jackTokenizer.equalTo("true") || jackTokenizer.equalTo("false") ||
                    jackTokenizer.equalTo("null") || jackTokenizer.equalTo("this")) {
                compileKeyWord(vmFile);
                switch (jackTokenizer.currentToken) {
                    case "true":
                        VMWriter.writePush(vmFile, "constant", 1);
                        VMWriter.writeArithmetic(vmFile, "neg");
                        break;
                    case "false":
                        VMWriter.writePush(vmFile, "constant", 0);
                        break;
                    case "null":
                        VMWriter.writePush(vmFile, "constant", 0);
                        break;
                    case "this":
                        VMWriter.writePush(vmFile, "pointer", 0);
                        break;
                }
            } else if (type.equals("symbol")) {
                if (jackTokenizer.equalTo("~") || jackTokenizer.equalTo("-")) {
                    String temp = jackTokenizer.currentToken;
                    jackTokenizer.preAdvance();
                    compileSymbol(temp, vmFile);
                    compileTerm(vmFile, classTable, subroutineTable);
                    switch (temp) {
                        case "-":
                            VMWriter.writeArithmetic(vmFile, "neg");
                            break;
                        case "~":
                            VMWriter.writeArithmetic(vmFile, "not");
                            break;
                    }
                } else if (jackTokenizer.equalTo("(")) {
                    jackTokenizer.preAdvance();
                    compileSymbol("(", vmFile);
                    compileExpression(vmFile, classTable, subroutineTable);
                    compileSymbol(")", vmFile);
                }
            } else if (type.equals("identifier")) {
                jackTokenizer.preAdvance();
                compileIdentifier(vmFile);
                String name = jackTokenizer.currentToken;
                if (jackTokenizer.hasMoreTokens()) {
                    jackTokenizer.advance();
                    if (jackTokenizer.equalTo("(") || jackTokenizer.equalTo(".")) {
                        //subroutineCall
                        jackTokenizer.preAdvance();
                        compileSubroutineCall(vmFile, name, classTable, subroutineTable);
                    } else {

                        writePushOrPopVar(vmFile, classTable, subroutineTable, name, false);
                        if (jackTokenizer.equalTo("[")) {
                            //varName'['expression']' case
                            jackTokenizer.preAdvance();
                            compileSymbol("[", vmFile);
                            compileExpression(vmFile, classTable, subroutineTable);
                            compileSymbol("]", vmFile);
                            VMWriter.writeArithmetic(vmFile, "add");
                            VMWriter.writePop(vmFile, "pointer", 1);
                            VMWriter.writePush(vmFile, "that", 0);
                        }
                        else jackTokenizer.preAdvance();
                    }
                }
            } else {
                jackTokenizer.preAdvance();
                return false;
            }
        }
        return true;
    }

    private static int compileExpressionList(FileWriter vmFile, SymbolTable classTable, SymbolTable subroutineTable) throws Exception {
        compileExpression(vmFile, classTable, subroutineTable);
        boolean moreExpressions = true;
        int count = 1;
        while (moreExpressions) {
            if (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                if (jackTokenizer.equalTo(",")) {
                    jackTokenizer.preAdvance();
                    compileSymbol(",", vmFile);
                    compileExpression(vmFile, classTable, subroutineTable);
                    count++;
                } else {
                    jackTokenizer.preAdvance();
                    moreExpressions = false;
                }
            } else moreExpressions = false;
        }
        return count;
    }


    /**
     * This function compile a class to xml file.
     *
     * @param inputJackTokenizer input file.
     * @param vmFile             output file.
     * @throws Exception
     */
    static void compileClass(JackTokenizer inputJackTokenizer, FileWriter vmFile) throws Exception {

        jackTokenizer = inputJackTokenizer;
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();

            if (!(jackTokenizer.equalTo("class"))) {
                throw new Exception("Must be class.");
            } else {


                jackTokenizer.advance(); //className and save it.
                String className = jackTokenizer.currentToken;
                SymbolTable classSymbolTable = new SymbolTable(className);

                compileSymbol("{", vmFile);

                while (compileClassVarDec(vmFile, classSymbolTable)) ; //classVarDec*

                while (compileSubroutine(vmFile, classSymbolTable, new SymbolTable(""), className))
                    ; //subroutineDec*

                compileSymbol("}", vmFile);

            }
        }
    }
}
