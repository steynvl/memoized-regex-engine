package pl.marcinchwedczuk.reng.parser;

import java.util.ArrayList;
import java.util.List;

public class RLexer {
    private final String input;
    private int curr;

    public RLexer(String input) {
        this.input = input;
        this.curr = 0;
    }

    public List<RToken> split() {
        int numGroups = 0;
        List<RToken> tokens = new ArrayList<>();

        while (curr < input.length()) {
            char c = input.charAt(curr);
            int cPos = curr;
            curr++;

            switch (c) {
                case '(':
                    char next = input.charAt(curr);
                    if (next == '?') {
                        curr++;
                        next = input.charAt(curr++);
                        if (next == '=') {
                            tokens.add(new RToken(RTokenType.POS_LOOKAHEAD, '=', cPos));
                        } else if (next == '!') {
                            tokens.add(new RToken(RTokenType.NEG_LOOKAHEAD, '!', cPos));
                        } else {
                            throw new RParseException(cPos, "Incomplete group structure.");
                        }
                    } else {
                        numGroups += 1;
                        tokens.add(new RToken(RTokenType.LPAREN, '(', cPos));
                    }
                    break;

                case ')':
                    tokens.add(new RToken(RTokenType.RPAREN, ')', cPos));
                    break;

                case '[':
                    tokens.add(new RToken(RTokenType.LGROUP, '[', cPos));
                    break;

                case ']':
                    tokens.add(new RToken(RTokenType.RGROUP, ']', cPos));
                    break;

                case '{':
                    tokens.add(new RToken(RTokenType.LRANGE, '{',  cPos));
                    break;

                case '}':
                    tokens.add(new RToken(RTokenType.RRANGE, '}',  cPos));
                    break;

                case '*':
                    tokens.add(new RToken(RTokenType.STAR, '*', cPos));
                    break;

                case '+':
                    tokens.add(new RToken(RTokenType.PLUS, '+',  cPos));
                    break;

                case '?':
                    tokens.add(new RToken(RTokenType.QMARK, '?', cPos));
                    break;

                case '|':
                    tokens.add(new RToken(RTokenType.ALTERNATIVE, '|', cPos));
                    break;

                case '.':
                    tokens.add(new RToken(RTokenType.MATCH_ANY, '.', cPos));
                    break;

                case '^':
                    tokens.add(new RToken(RTokenType.AT_BEGINNING, '^', cPos));
                    break;

                case '$':
                    tokens.add(new RToken(RTokenType.AT_END, '$', cPos));
                    break;

                case '\\':
                    // Escape sequence .e.g \$
                    if (curr > input.length())
                        throw new RParseException(cPos,
                                "Unexpected end of input inside escape sequence.");

                    char escape = input.charAt(curr);
                    curr++;

                    if (escape >= '1' && escape <= '9') {
                        int val = escape - '0';
                        if (val > numGroups) {
                            throw new RParseException(cPos,
                                    "This token references a non-existent or invalid subpattern");
                        }

                        tokens.add(new RToken(RTokenType.BACKREF, escape, cPos));
                        break;
                    }

                    switch (escape) {
                        // Standard escapes
                        case 'n':
                            tokens.add(new RToken(RTokenType.CHARACTER, '\n', cPos));
                            break;

                        case 'r':
                            tokens.add(new RToken(RTokenType.CHARACTER, '\r', cPos));
                            break;

                        case '\\':
                            tokens.add(new RToken(RTokenType.CHARACTER, '\\', cPos));
                            break;

                        // Regex escapes
                        case '(': case ')':
                        case '[': case ']':
                        case '{': case '}':
                        case '*': case '+': case '?': case '|': case '.':
                        case '^': case '$':
                            tokens.add(new RToken(RTokenType.CHARACTER, escape, cPos));
                            break;

                        default:
                            throw new RParseException(cPos,
                                    "Unknown escape sequence: '" + escape + "'.");
                    }
                    break;

                default:
                    // Normal characters
                    tokens.add(new RToken(RTokenType.CHARACTER, c, cPos));
                    break;
            }
        }

        tokens.add(new RToken(RTokenType.EOF, '\0', input.length()));
        return tokens;
    }
}
