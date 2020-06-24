/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2016 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2016 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.matchers.heuristic.gt;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.optimal.zs.ZsMatcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeMap;
import com.github.gumtreediff.utils.StringAlgorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleBottomUpMatcher extends Matcher {
    //TODO make final?
    public static int SIZE_THRESHOLD =
            Integer.parseInt(System.getProperty("gt.bum.szt", "1000"));
    public static final double SIM_THRESHOLD =
            Double.parseDouble(System.getProperty("gt.bum.smt", "0.5"));

    protected TreeMap srcIds;
    protected TreeMap dstIds;

    protected TreeMap mappedSrc;
    protected TreeMap mappedDst;

    public SimpleBottomUpMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
        srcIds = new TreeMap(src);
        dstIds = new TreeMap(dst);

        mappedSrc = new TreeMap();
        mappedDst = new TreeMap();
        for (Mapping m : store.asSet()) {
            mappedSrc.putTrees(m.getFirst());
            mappedDst.putTrees(m.getSecond());
        }
    }

    @Override
    public void match() {
        for (ITree t: src.postOrder())  {
            if (t.isRoot()) {
                addMapping(t, this.dst);
                lastChanceMatch(t, this.dst);
                break;
            } else if (!(isSrcMatched(t) || t.isLeaf())) {
                List<ITree> candidates = getDstCandidates(t);
                ITree best = null;
                double max = -1D;

                for (ITree cand: candidates) {
                    double sim = jaccardSimilarity(t, cand);
                    if (sim > max && sim >= SIM_THRESHOLD) {
                        if (t.getDepth() == cand.getDepth()) {
                            lastChanceMatch(t, best);
                            addMapping(t, best);
                            return;
                        }
                        max = sim;
                        best = cand;
                    }
                }

                if (best != null) {
                    lastChanceMatch(t, best);
                    addMapping(t, best);
                }
            }
        }
    }

    protected List<ITree> getDstCandidates(ITree src) {
        List<ITree> seeds = new ArrayList<>();
        for (ITree c: src.getDescendants()) {
            ITree m = mappings.getDst(c);
            if (m != null) seeds.add(m);
        }
        List<ITree> candidates = new ArrayList<>();
        Set<ITree> visited = new HashSet<>();
        for (ITree seed: seeds) {
            while (seed.getParent() != null) {
                ITree parent = seed.getParent();
                if (visited.contains(parent))
                    break;
                visited.add(parent);
                if (parent.getType() == src.getType() && !isDstMatched(parent) && !parent.isRoot())
                    candidates.add(parent);
                seed = parent;
            }
        }

        return candidates;
    }

    protected void lastChanceMatch(ITree src, ITree dst) {
        List<ITree> srcChildren = src.getChildren();
        List<ITree> dstChildren = dst.getChildren();

        List<int[]> lcs = StringAlgorithms.lcss(srcChildren, dstChildren);
        for (int[] x: lcs) {

            ITree t1 = srcChildren.get(x[0]);
            ITree t2 = dstChildren.get(x[1]);
            if (!(mappedSrc.contains(t1) || mappedDst.contains(t2)))
                addMapping(t1, t2);
        }
    }

    /**
     * Remove mapped nodes from the tree. Be careful this method will invalidate
     * all the metrics of this tree and its descendants. If you need them, you need
     * to recompute them.
     */
    public ITree removeMatched(ITree tree, boolean isSrc) {
        for (ITree t: tree.getTrees()) {
            if ((isSrc && isSrcMatched(t)) || ((!isSrc) && isDstMatched(t))) {
                if (t.getParent() != null) t.getParent().getChildren().remove(t);
                t.setParent(null);
            }
        }
        tree.refresh();
        return tree;
    }

    @Override
    public boolean isMappingAllowed(ITree src, ITree dst) {
        return src.hasSameType(dst)
                && !(isSrcMatched(src) || isDstMatched(dst));
    }

    @Override
    protected void addMapping(ITree src, ITree dst) {
        mappedSrc.putTree(src);
        mappedDst.putTree(dst);
        super.addMapping(src, dst);
    }

    boolean isSrcMatched(ITree tree) {
        return mappedSrc.contains(tree);
    }

    boolean isDstMatched(ITree tree) {
        return mappedDst.contains(tree);
    }
}
