package pl.marcinchwedczuk.reng;

import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;

public class RAst {
    public static final Long UNBOUND = Long.MAX_VALUE;
    public static int idCounter = 0;

    public final RAstType type;
    public final Set<Character> chars;
    public final List<RAst> exprs;

    /* repeat from to, both inclusive */
    public final long repeatMin;
    public final long repeatMax;

    /* used by backreferences */
    public final int captureGroup;

    /* unique identifier, used by memoisation function */
    public final int id;

    /* in degree of state */
    private int inDegree = 0;
    private boolean isAncestorNode = false;

    public RAst(RAstType type,
                Set<Character> chars,
                List<RAst> exprs,
                long repeatMin,
                long repeatMax,
                int captureGroup) {
        this.type = type;
        this.chars = chars;
        this.exprs = exprs;
        this.repeatMin = repeatMin;
        this.repeatMax = repeatMax;
        this.captureGroup = captureGroup;
        this.id = idCounter++;
    }

    public RAst(RAstType type,
                Set<Character> chars) {
        this(type,
             chars,
             Collections.emptyList(),
             -1, -1, -1);
    }

    public RAst(RAstType type,
                List<RAst> exprs) {
        this(type,
             Collections.emptySet(),
             exprs,
             -1, -1, -1);
    }

    public RAst(RAstType type, List<RAst> exprs, int captureGroup) {
        this(type,
             Collections.emptySet(),
             exprs,
             -1, -1, captureGroup);
    }

    public RAst headExpr() {
        return exprs.iterator().next();
    }

    public int getInDegree() {
        return inDegree;
    }

    public void setInDegree(int val) {
        this.inDegree = val;
    }

    public boolean getIsAncestorNode() {
        return isAncestorNode;
    }

    public void setIsAncestorNode(boolean val) {
        this.isAncestorNode = val;
    }

    @Override
    public String toString() {
        return toString(-1);
    }

    private String toString(int outsidePriority) {
        String tmp;

        switch (type) {
            case GROUP:
                if (chars.size() == 1) {
                    tmp = chars
                            .iterator().next()
                            .toString();
                }
                else {
                    tmp = chars.stream()
                        .sorted()
                        .map(Object::toString)
                        .collect(joining("", "[", "]"));
                }
                break;

            case NEGATED_GROUP:
                if (chars.isEmpty()) {
                    // Empty inverted group is used to represent `.` (any)
                    tmp = ".";
                }
                else {
                    tmp = chars.stream()
                            .sorted()
                            .map(Object::toString)
                            .collect(joining("", "[^", "]"));
                }
                break;

            case CAPTURE_GROUP:
                String reg = exprs.stream()
                        .map(e -> e.toString(RAstType.CAPTURE_GROUP.priority))
                        .collect(joining());

                tmp = addParentheses(reg);
                break;
            case REPEAT:
                tmp = toStringRepeat();
                break;

            case CONCAT:
                tmp = exprs.stream()
                        .map(e -> e.toString(RAstType.CONCAT.priority))
                        .collect(joining());
                break;

            case ALTERNATIVE:
                tmp = exprs.stream()
                        .map(e -> e.toString(RAstType.ALTERNATIVE.priority))
                        .collect(joining("|"));
                break;

            case AT_BEGINNING: tmp = "^"; break;
            case AT_END: tmp = "$"; break;

            default: throw new AssertionError("Unknown enum value: " + type);
        }

        return (outsidePriority > type.priority)
                ? addParentheses(tmp)
                : tmp;
    }

    private String toStringRepeat() {
        String inner = headExpr().toString(RAstType.REPEAT.priority);

        if (repeatMin == 0 && repeatMax == UNBOUND) {
            // A*
            return inner + "*";
        }
        else if (repeatMin == 1 && repeatMax == UNBOUND) {
            // A+
            return inner + "+";
        }
        else if (repeatMin == 0 && repeatMax == 1) {
            // A?
            return inner + "?";
        }
        else if (repeatMin == repeatMax) {
            // A{N}
            return inner + "{" + repeatMin + "}";
        }
        else {
            // A{N,M}
            String minStr = Long.toString(repeatMin);
            String maxStr = (repeatMax == UNBOUND) ? "" : Long.toString(repeatMax);
            return inner + "{" + minStr + "," + maxStr + "}";
        }
    }

    private static String addParentheses(String s) {
        return "(" + s + ")";
    }

    public static RAst group(char... chars) {
        return new RAst(RAstType.GROUP, toSet(chars));
    }

    public static RAst invGroup(char... chars) {
        return new RAst(RAstType.NEGATED_GROUP, toSet(chars));
    }

    public static RAst any() {
        // We represent . as inverted empty group.
        return new RAst(RAstType.NEGATED_GROUP, Collections.emptySet());
    }

    public static RAst concat(RAst... exprs) {
        return new RAst(RAstType.CONCAT, Arrays.asList(exprs));
    }

    public static RAst literal(String s) {
        RAst[] chars = s.chars()
                .mapToObj(c -> RAst.group((char) c))
                .toArray(RAst[]::new);

        return RAst.concat(chars);
    }

    public static RAst alternative(RAst... expr) {
        return new RAst(RAstType.ALTERNATIVE, Arrays.asList(expr));
    }

    public static RAst star(RAst expr) {
        return repeat(expr, 0, UNBOUND);
    }

    public static RAst plus(RAst expr) {
        return repeat(expr, 1, UNBOUND);
    }

    public static RAst backreference(char group) {
        return new RAst(RAstType.BACKREF, Collections.emptyList(), group - '0');
    }

    public static RAst posLookahead(RAst expr) {
        return new RAst(RAstType.POS_LOOKAHEAD, singletonList(expr));
    }

    public static RAst negLookahead(RAst expr) {
        return new RAst(RAstType.NEG_LOOKAHEAD, singletonList(expr));
    }

    public static RAst captureGroup(RAst expr, int captureGroup) {
        return new RAst(RAstType.CAPTURE_GROUP, singletonList(expr), captureGroup);
    }

    public static RAst repeat(RAst expr, long min, long max) {
        return new RAst(
                RAstType.REPEAT,
                emptySet(),
                singletonList(expr),
                min, max, -1);
    }

    public static RAst atBeginning() {
        return new RAst(
                RAstType.AT_BEGINNING,
                emptySet(),
                emptyList(),
                -1, -1, -1);
    }

    public static RAst atEnd() {
        return new RAst(
                RAstType.AT_END,
                emptySet(),
                emptyList(),
                -1, -1, -1);
    }

    /** Adds ^ and $ anchors to the regex r.
     */
    public static RAst fullMatch(RAst r) {
        return RAst.concat(
                atBeginning(),
                r,
                atEnd());
    }

    private static Set<Character> toSet(char[] chars) {
        Set<Character> set = new HashSet<>();

        for(char c: chars) {
            set.add(c);
        }

        return set;
    }
}
