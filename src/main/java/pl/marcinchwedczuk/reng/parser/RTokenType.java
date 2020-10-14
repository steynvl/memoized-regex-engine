package pl.marcinchwedczuk.reng.parser;

public enum RTokenType {
    // Parentheses e.g. (foo)
    LPAREN,
    RPAREN,

    // Character Groups e.g. [0-9]
    LGROUP,
    RGROUP,

    // Positive and Negative lookaheads e.g. a(?=b) a(?!b)
    POS_LOOKAHEAD,
    NEG_LOOKAHEAD,

    // Backreferences e.g. (\d+)\1
    BACKREF,

    // Ranges e.g. {1,2}
    LRANGE,
    RRANGE,

    // Metacharacters: . * + ? |
    MATCH_ANY,
    STAR,
    PLUS,
    QMARK,
    ALTERNATIVE,

    // Anchors: ^ and $
    AT_BEGINNING,
    AT_END,

    CHARACTER,

    // End of input
    EOF
}
