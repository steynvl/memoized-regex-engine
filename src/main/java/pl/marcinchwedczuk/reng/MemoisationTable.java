package pl.marcinchwedczuk.reng;

public interface MemoisationTable {

    /**
     * Get the value at the given cell in the memo table
     *
     * @param node state to get cell value of
     * @param wordIdx index in input string to match
     * @return has the cell been marked
     */
    boolean get(RAst node, int wordIdx);

    /**
     * Set the given cell to true in the memo table
     *
     * @param node state to mark in memo table
     * @param wordIdx index in input string to match
     */
    void mark(RAst node, int wordIdx);

}
