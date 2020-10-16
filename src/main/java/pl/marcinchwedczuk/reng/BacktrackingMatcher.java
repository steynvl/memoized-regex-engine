package pl.marcinchwedczuk.reng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("SimplifiableConditionalExpression")
public class BacktrackingMatcher {

    public static boolean[][] memoTable;

    public static Match match(String s, RAst regex) {
        Input input = Input.of(s);

        // maps capture group to matched text
        Map<Integer, String> groups = new HashMap<>();

        // memoisation table
        int rowSize = regex.id+1 > 0 ? regex.id+1 : 1;
        int colSize = s.length() > 0 ? s.length()+1 : 1;
        memoTable = new boolean[rowSize][colSize];

        while (true) {
            int startIndex = input.currentPos();
            AtomicInteger endIndex = new AtomicInteger(0);

            boolean hasMatch = match(input, regex, groups, () -> {
                endIndex.set(input.currentPos());
                return true;
            });

            if (hasMatch) {
                return new Match(s, true, startIndex, endIndex.get());
            }

            // We are at the end of the input - no match
            if (input.atEnd()) return new Match(s, false, -1, -1);

            // Try to match from next index
            input.advance(1);
        }
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    public static boolean match(Input input, RAst ast,
                                Map<Integer, String> groups, Cont cont) {
        RAstType type = ast.type;
        InputPositionMarker m;

        if (memoTable[ast.id][input.currentPos()]) {
            return false;
        }
        memoTable[ast.id][input.currentPos()] = true;

        switch (type) {
            case AT_BEGINNING:
                return input.atBeginning()
                    ? cont.run()
                    : false;

            case AT_END:
                return input.atEnd()
                    ? cont.run()
                    : false;

            case GROUP:
                if (input.atEnd()) return false;

                if (ast.chars.contains(input.current())) {
                    m = input.markPosition();
                    input.advance(1);
                    try {
                        return cont.run();
                    } finally {
                        input.restorePosition(m);
                    }
                }
                return false;

            case NEGATED_GROUP:
                if (input.atEnd()) return false;
                if (!ast.chars.contains(input.current())) {
                    m = input.markPosition();
                    input.advance(1);
                    try {
                        return cont.run();
                    }
                    finally {
                        input.restorePosition(m);
                    }
                }
                return false;


            case CAPTURE_GROUP:
                m = input.markPosition();
                return match(input, ast.headExpr(), groups, () -> {
                    groups.put(ast.captureGroup, input.range(m.pos, input.currentPos()));
                    return cont.run();
                });
            case BACKREF:
                return backreferenceRec(input, ast, groups, cont);

            case POS_LOOKAHEAD:
                m = input.markPosition();
                if (!match(input, ast.headExpr(), groups, cont))
                    return false;

                input.restorePosition(m);
                return cont.run();

            case NEG_LOOKAHEAD:
                m = input.markPosition();
                if (match(input, ast.headExpr(), groups, cont))
                    return false;

                input.restorePosition(m);
                return cont.run();

            case CONCAT:
                return concatRec(input, ast.exprs, 0, groups, cont);

            case ALTERNATIVE:
                return alternativeRec(input, ast.exprs, 0, groups, cont);

            case REPEAT:
                return repeatRec(input, ast, 0, groups, cont);

            default: throw new AssertionError("Unknown enum value: " + type);
        }
    }

    private static boolean backreferenceRec(Input input,
                                            RAst ast,
                                            Map<Integer, String> groups,
                                            Cont cont) {
        assert ast.exprs.size() == 0;
        assert ast.captureGroup >= 1 && ast.captureGroup <= 9;

        String text = groups.get(ast.captureGroup);
        for (int i = 0; i < text.length(); i++) {
            if (input.atEnd()) return false;
            if (input.current() != text.charAt(i)) return false;
            input.advance(1);
        }

        return cont.run();
    }

    private static boolean concatRec(Input input,
                                     List<RAst> exprs,
                                     int currExpr,
                                     Map<Integer, String> groups,
                                     Cont cont) {
        if (currExpr == exprs.size()) {
            return cont.run();
        }

        // Match exprs.get(currExpr)
        return match(input, exprs.get(currExpr), groups, () ->
            // If it succeeded then match next expression
            concatRec(input, exprs, currExpr + 1, groups, cont)
        );
    }

    private static boolean repeatRec(Input input,
                                     RAst repeatAst,
                                     long matchCount,
                                     Map<Integer, String> groups,
                                     Cont cont) {
        if (matchCount > repeatAst.repeatMax)
            return false;

        boolean matched = match(input, repeatAst.headExpr(), groups, () ->
            repeatRec(input, repeatAst, matchCount+1, groups, cont)
        );

        if (!matched && (matchCount >= repeatAst.repeatMin)) {
            // r{N} did not match.
            // Here we are matching r{N-1}, we are sure it is matching
            // because this function was called.
            return cont.run();
        }

        // r{N} matched?
        return matched;
    }

    private static boolean alternativeRec(Input input,
                                          List<RAst> expr,
                                          int currExpr,
                                          Map<Integer, String> groups,
                                          Cont cont) {
        if (currExpr == expr.size()) {
            // We tried all alternatives but achieved no match.
            return false;
        }

        boolean matched = match(input, expr.get(currExpr), groups, cont);
        if (matched) return true;

        // Let's try next alternative "branch"
        return alternativeRec(input, expr, currExpr+1, groups, cont);
    }
}
