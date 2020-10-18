package pl.marcinchwedczuk.reng;

import java.util.HashMap;
import java.util.Map;

public class HashTable implements MemoisationTable {

    private Map<Pair<Integer, Integer>, Boolean> table;

    public HashTable(int numOfNodes, int numOfChars) {
        assert numOfNodes >= 0 && numOfChars >= 0;
        table = new HashMap<>();
    }

    @Override
    public boolean get(RAst node, int wordIdx) {
        return table.getOrDefault(new Pair<>(node.id, wordIdx), false);
    }

    @Override
    public void mark(RAst node, int wordIdx) {
        table.put(new Pair<>(node.id, wordIdx), true);
    }
}
