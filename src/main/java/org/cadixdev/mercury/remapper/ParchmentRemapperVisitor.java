package org.cadixdev.mercury.remapper;

import org.cadixdev.mercury.ParchmentTree;
import org.cadixdev.mercury.RewriteContext;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class ParchmentRemapperVisitor extends ASTVisitor {

    final RewriteContext context;
    final ParchmentTree tree;

    public ParchmentRemapperVisitor(RewriteContext context, ParchmentTree tree) {
        this.context = context;
        this.tree = tree;
    }
    final void updateIdentifier(SimpleName node, String newName) {
        if (!node.getIdentifier().equals(newName) && !node.isVar()) {
            this.context.createASTRewrite().set(node, SimpleName.IDENTIFIER_PROPERTY, newName, null);
        }
    }
    final void updateJavadoc(MethodDeclaration node, Javadoc javadoc) {
        this.context.createASTRewrite().set(node, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);
    }
    @SuppressWarnings("unchecked")
    final Javadoc createMethodJavadoc(AST ast, String summery, List<TagElement> params) {
        var jdoc = ast.newJavadoc();

        var descTag = ast.newTagElement();
        var text = ast.newTextElement();
        text.setText(summery);
        descTag.fragments().add(text);

        jdoc.tags().add(descTag);
        jdoc.tags().addAll(params);
        return jdoc;

    }


    @Override
    public boolean visit(MethodDeclaration node) {
        var binding = node.resolveBinding();
        var clazz = binding.getDeclaringClass();
        if (clazz == null) return false;

        var className = clazz.getBinaryName();

        var parchmentMethod = tree.getMethod(className, binding.getName(), RemapperVisitor.methodDesc(binding));
        if (parchmentMethod == null) return false;

        List<String> jdoc = parchmentMethod.javadoc().map(ParchmentTree.Javadoc::data).orElseGet(List::of);
        String jdocStr = String.join("\n", jdoc);

        // TODO: PARAMS

        updateJavadoc(node, createMethodJavadoc(node.getAST(), jdocStr, List.of()));

        return false;
    }
}
