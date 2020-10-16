package pl.marcinchwedczuk.reng;

public class BacktrackingPoint {

    private final int state;
    private final int index;

    public BacktrackingPoint(int state, int index) {
        this.state = state;
        this.index = index;
    }

    public int getState() {
        return state;
    }

    public int getIndex() {
        return index;
    }

}
