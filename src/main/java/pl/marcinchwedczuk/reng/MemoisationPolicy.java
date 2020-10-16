package pl.marcinchwedczuk.reng;

public enum MemoisationPolicy {

    /* do not perform any memoisation */
    NONE,

    /* perform memoisation on all states  */
    ALL,

    /* states with in degree greater than 1 */
    IN_DEGREE_GREATER_THAN_1,

    /* states that are ancestor nodes */
    ANCESTOR_NODES

}
