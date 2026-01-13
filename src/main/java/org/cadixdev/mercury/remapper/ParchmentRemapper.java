package org.cadixdev.mercury.remapper;

import org.cadixdev.mercury.ParchmentTree;
import org.cadixdev.mercury.RewriteContext;
import org.cadixdev.mercury.SourceRewriter;

public class ParchmentRemapper implements SourceRewriter {

    private final ParchmentTree tree;

    public ParchmentRemapper(ParchmentTree tree) {
        this.tree = tree;
    }

    @Override
    public void rewrite(RewriteContext context) throws Exception {
        context.getCompilationUnit().accept(new ParchmentRemapperVisitor(context, this.tree));
    }
}
