package pl.marcinchwedczuk.reng;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MemoisationPolicyHelper {

    public static List<Integer> determineNodesToMemoise(RAst ast, MemoisationPolicy memPolicy) {

        switch (memPolicy) {
            case NONE:
                return new ArrayList<>();
            case ALL:
                return findAllNodes(ast);
            case IN_DEGREE_GREATER_THAN_1:
                calculateInDegreeAndAncestorNodes(ast);
                return new ArrayList<>();
            case ANCESTOR_NODES:
                calculateInDegreeAndAncestorNodes(ast);
                return new ArrayList<>();
            default:
                throw new RuntimeException("Unknown MemoisationPolicy [" + memPolicy.name() + "]");
        }
    }

    private static void calculateInDegreeAndAncestorNodes(RAst node) {
        calculateInDegreeAndAncestorNodes(node, new Stack<>());
    }

    private static void calculateInDegreeAndAncestorNodes(RAst node, Stack<Integer> alternations) {
        if (node.type == RAstType.CONCAT) {
            for (int i = 0; i < node.exprs.size(); i++) {
                RAst child = node.exprs.get(i);
                calculateInDegreeAndAncestorNodes(child, alternations);
                if (!alternations.empty()) {
                    assert alternations.size() == 1;
                    if (i + 1 < node.exprs.size()) {
                        RAst n = node.exprs.get(i + 1);
                        n.setInDegree(n.getInDegree() + alternations.pop());
                    } else {
                        alternations.pop();
                    }
                }
            }
            return;
        } else if (node.type == RAstType.ALTERNATIVE) {
            alternations.push(node.exprs.size());
        }

        for (RAst child : node.exprs) {
            calculateInDegreeAndAncestorNodes(child, alternations);
        }
    }

    private static List<Integer> findAllNodes(RAst ast) {
        List<Integer> ids = new ArrayList<>();
        ids.add(ast.id);
        System.out.println(ast.type + " [" + ast.id + "]" + "[in=" + ast.getInDegree() + "]");
        for (RAst child : ast.exprs) {
            ids.addAll(findAllNodes(child));
        }
        return ids;
    }

}
