/*
 * Copyright 2013-2018 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreeFactory;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.BreakIterator;
import java.util.List;

import static javax.tools.StandardLocation.SOURCE_PATH;

class AsciiDocTrees extends DocTrees {

    private final AsciidoctorConverter converter;
    private final StandardJavaFileManager fileManager;
    private final DocTrees docTrees;
    private final Field elementsField;

    AsciiDocTrees(AsciidoctorConverter converter, StandardJavaFileManager fileManager, DocTrees docTrees) {
        this.converter = converter;
        this.fileManager = fileManager;
        this.docTrees = docTrees;
        try {
            this.elementsField = docTrees.getClass().getDeclaredField("elements");
            this.elementsField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BreakIterator getBreakIterator() {
        return docTrees.getBreakIterator();
    }

    @Override
    public String getDocComment(TreePath path) {
        return converter.convert(docTrees.getDocComment(path));
    }

    @Override
    public DocCommentTree getDocCommentTree(TreePath path) {
        // First we convert the asciidoctor to HTML inside the AST.
        JCTree.JCCompilationUnit cu = (JCTree.JCCompilationUnit) path.getCompilationUnit();
        LazyDocCommentTableProcessor.processComments(cu.docComments, this::convertToAsciidoctor);

        // Then we allow the normal javadoc parsing to continue on the asciidoctor result.
        return docTrees.getDocCommentTree(path);
    }

    private Tokens.Comment convertToAsciidoctor(Tokens.Comment comment) {
        String javadoc = comment.getText();
        String asciidoc = converter.convert(javadoc);
        AsciidocComment result = new AsciidocComment(asciidoc, comment);
        System.err.println("");
        return result;
    }

    @Override
    public DocCommentTree getDocCommentTree(Element e) {
        TreePath path = getPath(e);
        if (path == null) {
            return null;
        }
        return getDocCommentTree(path);
    }

    @Override
    public DocCommentTree getDocCommentTree(FileObject fileObject) {
        // Empty names are used for built-in headers and footers, which need no asciidoctor processing anyway.
        if (!fileObject.getName().isEmpty() && !(fileObject instanceof AsciidocFileView)) {
            return docTrees.getDocCommentTree(new AsciidocFileView(converter, fileObject));
        }
        return docTrees.getDocCommentTree(fileObject);
    }

    @Override
    public DocCommentTree getDocCommentTree(Element e, String relativePath) throws IOException {
        PackageElement pkg = getElements().getPackageOf(e);
        JavaFileManager fileManager = getFileManager();
        FileObject input = fileManager.getFileForInput(SOURCE_PATH, pkg.getQualifiedName().toString(), relativePath);
        if (input == null) {
            throw new FileNotFoundException(relativePath);
        }
        return getDocCommentTree(input);
    }

    private JavacElements getElements() {
        try {
            return (JavacElements) elementsField.get(docTrees);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JavaFileManager getFileManager() {
        return fileManager;
    }

    @Override
    public DocTreePath getDocTreePath(FileObject fileObject, PackageElement packageElement) {
        return docTrees.getDocTreePath(fileObject, packageElement);
    }

    @Override
    public Element getElement(DocTreePath path) {
        return docTrees.getElement(path);
    }

    @Override
    public List<DocTree> getFirstSentence(List<? extends DocTree> list) {
        return docTrees.getFirstSentence(list);
    }

    @Override
    public DocSourcePositions getSourcePositions() {
        return docTrees.getSourcePositions();
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, DocTree t, DocCommentTree c, CompilationUnitTree root) {
        docTrees.printMessage(kind, msg, t, c, root);
    }

    @Override
    public void setBreakIterator(BreakIterator breakiterator) {
        docTrees.setBreakIterator(breakiterator);
    }

    @Override
    public DocTreeFactory getDocTreeFactory() {
        return docTrees.getDocTreeFactory();
    }


    public String getCharacters(EntityTree tree) {
        // FIXME placeholder implementation
        return tree.toString();
    }

    @Override
    public Tree getTree(Element element) {
        return docTrees.getTree(element);
    }

    @Override
    public ClassTree getTree(TypeElement element) {
        return docTrees.getTree(element);
    }

    @Override
    public MethodTree getTree(ExecutableElement method) {
        return docTrees.getTree(method);
    }

    @Override
    public Tree getTree(Element e, AnnotationMirror a) {
        return docTrees.getTree(e, a);
    }

    @Override
    public Tree getTree(Element e, AnnotationMirror a, AnnotationValue v) {
        return docTrees.getTree(e, a, v);
    }

    @Override
    public TreePath getPath(CompilationUnitTree unit, Tree node) {
        return docTrees.getPath(unit, node);
    }

    @Override
    public TreePath getPath(Element e) {
        return docTrees.getPath(e);
    }

    @Override
    public TreePath getPath(Element e, AnnotationMirror a) {
        return docTrees.getPath(e, a);
    }

    @Override
    public TreePath getPath(Element e, AnnotationMirror a, AnnotationValue v) {
        return docTrees.getPath(e, a, v);
    }

    @Override
    public Element getElement(TreePath path) {
        return docTrees.getElement(path);
    }

    @Override
    public TypeMirror getTypeMirror(TreePath path) {
        return docTrees.getTypeMirror(path);
    }

    @Override
    public Scope getScope(TreePath path) {
        return docTrees.getScope(path);
    }

    @Override
    public boolean isAccessible(Scope scope, TypeElement type) {
        return docTrees.isAccessible(scope, type);
    }

    @Override
    public boolean isAccessible(Scope scope, Element member, DeclaredType type) {
        return docTrees.isAccessible(scope, member, type);
    }

    @Override
    public TypeMirror getOriginalType(ErrorType errorType) {
        return docTrees.getOriginalType(errorType);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Tree t, CompilationUnitTree root) {
        docTrees.printMessage(kind, msg, t, root);
    }

    @Override
    public TypeMirror getLub(CatchTree tree) {
        return docTrees.getLub(tree);
    }
}
