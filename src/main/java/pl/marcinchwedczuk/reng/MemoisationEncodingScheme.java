package pl.marcinchwedczuk.reng;

public enum MemoisationEncodingScheme {

    /*  2-d array */
    BIT_MAP,

    /* no negative entries when using hash table */
    HASH_TABLE,

    /* run-length encoding */
    RLE

}
