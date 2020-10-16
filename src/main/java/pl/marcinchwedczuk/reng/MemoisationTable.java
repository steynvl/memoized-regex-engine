package pl.marcinchwedczuk.reng;

public abstract class MemoisationTable {

    /**
     *
     * @param stateId RAst.id
     * @param wordIdx index in input string to match
     * @return has the cell been marked
     */
    public abstract boolean get(int stateId, int wordIdx);

    public abstract boolean mark(int stateId, int wordIdx);

}
