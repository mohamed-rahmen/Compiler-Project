
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {

    //Lexical Elements
    static final String KEYWORD = "(class)|(constructor)|(function)|(method)|(field)|(static)|(var)|" +
            "(int)|(char)|(boolean)|(void)|(true)|(false)|(null)|(this)|(let)|" +
            "(do)|(if)|(else)|(while)|(return)";
    static final String SYMBOL = "(\\{)|(\")|(\\})|(\\()|(\\))|(\\[)|(\\])|(\\.)|(\\,)|(\\;)|(\\+)|(\\-)|" +
            "(\\*)|(\\/)|(\\&)|(\\|)|(\\<)|(\\>)|(\\=)|(\\~)";
    static final String IDENTIFIER = "([a-zA-z_][\\w]*)";
    static final String STRING_CONSTANT = "(\"[^\"\n]*\")";
    static final String INT_CONSTANT = "([\\d]+)";

    //Program Structure

    //Type to use for engine
    private String[] TYPES_REGEX = {KEYWORD, STRING_CONSTANT, IDENTIFIER, INT_CONSTANT, SYMBOL};
    private String[] TYPES = {"keyword", "stringConstant", "identifier", "integerConstant", "symbol"};
    private RandomAccessFile jackFile;
    private ArrayList<String> tokens;
    private int counterAdv = 0;
    String currentToken = " ";
    private String curType = " ";

    /**
     * This is the constructor method.
     */
    public JackTokenizer(RandomAccessFile jackfile) {
        this.jackFile = jackfile;
        tokens = new ArrayList<String>();
        readAll();
    }

    /**
     * This method is doing the job of the main method .
     */
    private void readAll() {
        String line;
        String[] checkLine = new String[]{"", "0"};
        boolean flag = true;
        try {
            while ((line = jackFile.readLine()) != null) {
                flag = checkComOrLine(line, checkLine, flag);
                if (flag || checkLine[1].equals("1")) {
                    if (!checkLine[0].equals(" ")) {
                        String[] splits = splitLine(checkLine[0]);
                        for (String token : splits) {
                            insertTokens(token);
                        }
                        checkLine[0] = " ";
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * check if there is set of symbols or xxSYMBOLxx.
     */
    private void checkSymbol(int found, String token) {
        Pattern p;
        Matcher m;
        if (found == 0) {
            p = Pattern.compile(SYMBOL);
            m = p.matcher(token);
            int index = 0, prevEnd = 0;
            boolean constantString = true, beInString = false;
            int stringIndex = 0;
            while (m.find()) {
                String symbol = token.substring(m.start(), m.start() + 1);
                if (symbol.equals("\"")) {
                    if (constantString) {
                        constantString = false;
                        beInString = true;
                        index = m.start();
                    } else {
                        constantString = true;
                    }
                }

                if (!constantString) {
                    continue;
                }
                if (prevEnd != m.start()) {  //in case of continuous set of symbols
                    int i = 0;
                    if (beInString) i++;
                    tokens.add(token.substring(index, m.start() + i));
                }
                if (beInString) {
                    beInString = false;
                    prevEnd = m.end();
                    index = m.end();
                    continue;
                }
                tokens.add(token.substring(m.start(), m.end()));
                prevEnd = m.end();
                index = m.end();
            }

            if (prevEnd != token.length())// deals with xxxSYMBOLxxx
            {
                tokens.add(token.substring(prevEnd, token.length()));
            }
        }
    }

    /**
     * Responsible for inserting the tokens into the tokens list with doing
     * "the last touch" on the tokens to separate them perfectly
     */
    private void insertTokens(String token) {

        int found = 0;
        for (String regex : TYPES_REGEX) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(token);
            if (m.matches()) {
                tokens.add(token);
//                if (m.end())
                found = 1;
                break;
            }
        }
        checkSymbol(found, token);
    }

    /**
     * This method is deleting the comments from the current line .
     *
     * @param line
     * @param checkLine
     * @param flag
     * @return
     */
    private boolean checkComOrLine(String line, String[] checkLine, boolean flag) {
        int commentBeg = line.indexOf("/*");
        int comment = line.indexOf("//");
        int commentEnd = line.indexOf("*/");
        int commentStr = line.indexOf(" \" ");
        int comentStrEnd = line.lastIndexOf("\"");

        if (commentBeg != -1) {
            flag = false;
        }

        if (!flag) {

            if (commentBeg != -1) {
                if (commentEnd == -1) {
                    checkLine[0] = line.substring(0, commentBeg);
                    return false;
                }
                if (commentStr != -1 && comentStrEnd != -1) {
                    if (commentBeg > commentStr && commentBeg < comentStrEnd && comment == -1) {
                        checkLine[0] = line;
                        return true;
                    }

                }
                if (checkLine[1].equals("1")) { //in case there is /* in the comments.
                    return false;
                }
                if (comment < commentBeg) { // in case some code // /* ....
                    checkLine[0] = line.substring(0, comment + 1);

                    return true;
                }
//                if (commentEnd != -1) {
//                    // in case  some code /* */ some code.
//                    return true;
//
//                }
                else { // in case  some code /** ... in new line the */
                    checkLine[0] = line.substring(0, commentBeg) + line.substring(commentEnd + 2, line.length() - 1);
                    return true;
                }

            }

            if (commentEnd != -1) {
                //checkLine[1]="0";
                if (commentStr != -1 && comentStrEnd != -1) {
                    if (commentEnd > commentStr && commentEnd < comentStrEnd && comment == -1) {
                        checkLine[0] = line;
                        return true;
                    }
                    if (commentEnd < line.length()) { // in case  */ some code .
                        checkLine[0] = line.substring(commentEnd + 2, line.length() - 1);
                        return true;
                    }
                    checkLine[1] = "0"; // in case */ end line .
                    return false;
                } else {
                    if (checkLine[1].equals("1")) { // in case into the comments .
                        return false;
                    }
                    return true;

                }

            }
        } else {

            if (comment == -1) { // in case there is no comment in the line .
                checkLine[0] = line;
                return true;
            }
            if (comment < commentEnd && comment > commentStr) {
                checkLine[0] = line;
                return true;
            }

            checkLine[0] = line.substring(0, comment); // in case some code and //
            return true;
        }
        return false;

    }

    /**
     * This method checks if there is more tokens .
     */
    public boolean hasMoreTokens() {

        return counterAdv < tokens.size();

    }


    /**
     * Splits the Line (after deleting the comments) based on space where spaces
     * inside a String constant aren't considered.
     */
    private static String[] splitLine(String line) {
        Pattern regex = Pattern.compile("\"[^\"]*\"|(\\s)");
        Pattern constantStringRegex = Pattern.compile(STRING_CONSTANT);
        Matcher m = regex.matcher(line);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            if (m.group(1) != null) m.appendReplacement(b, "Split");
            else m.appendReplacement(b, m.group(0));
        }
        m.appendTail(b);
        String replaced = b.toString();
        String[] splits = replaced.split("Split");
        return splits;
    }

    /**
     * This method is returning the type of the current token.
     */
    private void getType() {
        Pattern p;
        Matcher m;
        int typeCounter = 0;
        for (String type : TYPES_REGEX) {

            p = Pattern.compile(type);
            m = p.matcher(currentToken);
            if (m.matches()) {

                curType = TYPES[typeCounter];
                break;
            }
            typeCounter++;

        }


    }

    /**
     * This method is putting the current token to the next one .
     */
    void advance() {
        if (hasMoreTokens()) {

            currentToken = tokens.get(counterAdv);
            getType();
            counterAdv++;
        }

    }

    /**
     * This method is putting the current token to the prev one .
     */
    void preAdvance() {
        if (counterAdv >= 1) {
            currentToken = tokens.get(counterAdv - 2);
            getType();
            counterAdv--;
        }
    }

    /**
     * Returns the current token's type
     */
    String tokenType() {
        return curType;
    }

    /**
     * Function equal the current token with another string.
     *
     * @param token to compare with.
     * @return true if equals, false otherwise.
     */
    boolean equalTo(String token) {
        return this.currentToken.equals(token);
    }
}
