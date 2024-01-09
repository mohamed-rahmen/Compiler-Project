import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

    //Kinds of symbols.
    private final static String STATIC = "static";
    private final static String FIELD = "field";
    final static String ARG = "argument";
    final static String VAR = "local";
    private final static String NONE = "none"; // identifier is unknown in the current scope.

    //Index of symbol properties in the Array.
    private final static int INDEX_OF_KIND = 0;
    private final static int INDEX_OF_TYPE = 1;
    private final static int INDEX_OF_COUNTER = 2;
    final static int NOT_FOUND = -1;


    /** The counters for the symbol table. */
    private int staticCounter, localCounter, argumentCounter, fieldCounter;

    /** The container of the table elements. */
    private HashMap<String, ArrayList<String>> symbolTable;

    String name;

    /**
     * Construct a new symbol table.
     */
    SymbolTable(String name){
        this.name = name;
        //Start the counters with 0.
        this.staticCounter = this.localCounter = this.argumentCounter = this.fieldCounter = 0;

        //Construct the container.
        this.symbolTable = new HashMap<String, ArrayList<String>>();
    }

    void startSubroutine(){
        //reset the counters to 0.
        this.staticCounter = this.localCounter = this.argumentCounter = this.fieldCounter = 0;
        //clear the symbols in the table.
        this.symbolTable.clear();
    }

    /**
     * Add a new symbol to the table.
     * @param name symbol name.
     * @param type symbol type.
     * @param kind symbol kind.
     */
    void define(String name, String type, String kind){

        //Get the counter.
        int counter = varCount(kind, true);

        ArrayList<String > newName = new ArrayList<String>();
        newName.add(kind); // add kind to the symbol's line.
        newName.add(type); // add type to the symbol's line.
        newName.add(((Integer)counter).toString()); // add counter to the symbol's line.

        this.symbolTable.put(name, newName); // add the symbol(name) and it's properties to the table.
    }

    /**
     * Function return the number of variables of the given kind already defined in the current scope.
     * And increase the counter if we want.
     * @param kind to get it's counter
     * @param increaseCase true if we want to increase the counter, false otherwise.
     * @return the counter.
     */
    int varCount(String kind, boolean increaseCase){
        int counter = NOT_FOUND;
        switch (kind){
            case STATIC:
                counter = staticCounter;
                if (increaseCase) staticCounter++;
                break;
            case FIELD:
                counter = fieldCounter;
                if (increaseCase) fieldCounter++;
                break;
            case ARG:
                counter = argumentCounter;
                if (increaseCase) argumentCounter++;
                break;
            case VAR:
                counter = localCounter;
                if (increaseCase) localCounter++;
                break;
        }
        return counter;
    }

    /**
     * @param name of the symbol to get it's kind.
     * @return the kind of the named identifier in the current scope. Returns NONE if the
     * identifier is unknown in the current scope.
     */
    String kindOf(String name){
        if (!this.symbolTable.containsKey(name)) return NONE;
        return this.symbolTable.get(name).get(INDEX_OF_KIND);
    }

    /**
     * @param name of the symbol to get it's type.
     * @return the type of the named identifier in the current scope.
     */
    String typeOf(String name){
        if (!this.symbolTable.containsKey(name)) return NONE;
        return this.symbolTable.get(name).get(INDEX_OF_TYPE);
    }

    /**
     *
     * @param name of the symbol to get it's index.
     * @return the index assigned to named identifier
     */
    int indexOf(String name){
        if (!this.symbolTable.containsKey(name)) return NOT_FOUND;
        return Integer.parseInt(this.symbolTable.get(name).get(INDEX_OF_COUNTER));
    }
}


