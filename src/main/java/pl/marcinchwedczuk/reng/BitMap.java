package pl.marcinchwedczuk.reng;

public class BitMap implements MemoisationTable {

    private boolean[][] bitMap;

    public BitMap(int numOfNodes, int numOfChars) {
        assert numOfNodes >= 0 && numOfChars >= 0;
        bitMap = new boolean[numOfNodes][numOfChars];
    }

    @Override
    public boolean get(RAst node, int wordIdx) {
        try {
            return bitMap[node.getIndexInBitMap()][wordIdx];
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public void mark(RAst node, int wordIdx) {
        try {
            bitMap[node.getIndexInBitMap()][wordIdx] = true;
        } catch (ArrayIndexOutOfBoundsException e) { }
    }
}
