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
package org.asciidoclet.asciidoclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
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
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.internal.tool.DocEnvImpl;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
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

public class AsciidoctorFilteredEnvironment extends DocEnvImpl implements DocletEnvironment, AutoCloseable
{
    private final DocletEnvironment delegate;
    private final Reporter reporter;
    private final AsciidoctorRenderer renderer;
    private final AsciiDocTrees asciiDocTrees;

    public AsciidoctorFilteredEnvironment( DocletEnvironment environment, Reporter reporter, AsciidoctorRenderer renderer )
    {
        super( ((DocEnvImpl) environment).toolEnv, ((DocEnvImpl) environment).etable );
        this.delegate = environment;
        this.reporter = reporter;
        this.renderer = renderer;
        this.asciiDocTrees = new AsciiDocTrees( environment.getDocTrees() );
    }

    @Override
    public DocTrees getDocTrees()
    {
        return asciiDocTrees;
    }

    @Override
    public void close()
    {
        renderer.cleanup();
    }

    private class AsciiDocTrees extends DocTrees
    {
        private final DocTrees docTrees;

        AsciiDocTrees( DocTrees docTrees )
        {
            this.docTrees = docTrees;
        }

        public BreakIterator getBreakIterator()
        {
            return docTrees.getBreakIterator();
        }

        public DocCommentTree getDocCommentTree( TreePath path )
        {
            // First we convert the asciidoctor to HTML inside the AST.
            JCTree.JCCompilationUnit cu = (JCTree.JCCompilationUnit) path.getCompilationUnit();
            LazyDocCommentTableProcessor.processComments( cu.docComments, this::convertToAsciidoctor );

            // Then we allow the normal javadoc parsing to continue on the asciidoctor result.
            return docTrees.getDocCommentTree( path );
        }

        private Tokens.Comment convertToAsciidoctor( Tokens.Comment comment )
        {
            String javadoc = comment.getText();
            String asciidoc = renderer.render( javadoc, false );
            AsciidocComment result = new AsciidocComment( asciidoc, comment );;
            return result;
        }

        public DocCommentTree getDocCommentTree( Element e )
        {
            return docTrees.getDocCommentTree( e );
        }

        public DocCommentTree getDocCommentTree( FileObject fileObject )
        {
            return docTrees.getDocCommentTree( fileObject );
        }

        public DocCommentTree getDocCommentTree( Element e, String relativePath ) throws IOException
        {
            return docTrees.getDocCommentTree( e, relativePath );
        }

        public DocTreePath getDocTreePath( FileObject fileObject, PackageElement packageElement )
        {
            return docTrees.getDocTreePath( fileObject, packageElement );
        }

        public Element getElement( DocTreePath path )
        {
            return docTrees.getElement( path );
        }

        public List<DocTree> getFirstSentence( List<? extends DocTree> list )
        {
            return docTrees.getFirstSentence( list );
        }

        public DocSourcePositions getSourcePositions()
        {
            return docTrees.getSourcePositions();
        }

        public void printMessage( Diagnostic.Kind kind, CharSequence msg, DocTree t, DocCommentTree c, CompilationUnitTree root )
        {
            docTrees.printMessage( kind, msg, t, c, root );
        }

        public void setBreakIterator( BreakIterator breakiterator )
        {
            docTrees.setBreakIterator( breakiterator );
        }

        public DocTreeFactory getDocTreeFactory()
        {
            return docTrees.getDocTreeFactory();
        }

        public Tree getTree( Element element )
        {
            return docTrees.getTree( element );
        }

        public ClassTree getTree( TypeElement element )
        {
            return docTrees.getTree( element );
        }

        public MethodTree getTree( ExecutableElement method )
        {
            return docTrees.getTree( method );
        }

        public Tree getTree( Element e, AnnotationMirror a )
        {
            return docTrees.getTree( e, a );
        }

        public Tree getTree( Element e, AnnotationMirror a, AnnotationValue v )
        {
            return docTrees.getTree( e, a, v );
        }

        public TreePath getPath( CompilationUnitTree unit, Tree node )
        {
            return docTrees.getPath( unit, node );
        }

        public TreePath getPath( Element e )
        {
            return docTrees.getPath( e );
        }

        public TreePath getPath( Element e, AnnotationMirror a )
        {
            return docTrees.getPath( e, a );
        }

        public TreePath getPath( Element e, AnnotationMirror a, AnnotationValue v )
        {
            return docTrees.getPath( e, a, v );
        }

        public Element getElement( TreePath path )
        {
            return docTrees.getElement( path );
        }

        public TypeMirror getTypeMirror( TreePath path )
        {
            return docTrees.getTypeMirror( path );
        }

        public Scope getScope( TreePath path )
        {
            return docTrees.getScope( path );
        }

        public String getDocComment( TreePath path )
        {
            return docTrees.getDocComment( path );
        }

        public boolean isAccessible( Scope scope, TypeElement type )
        {
            return docTrees.isAccessible( scope, type );
        }

        public boolean isAccessible( Scope scope, Element member, DeclaredType type )
        {
            return docTrees.isAccessible( scope, member, type );
        }

        public TypeMirror getOriginalType( ErrorType errorType )
        {
            return docTrees.getOriginalType( errorType );
        }

        public void printMessage( Diagnostic.Kind kind, CharSequence msg, Tree t, CompilationUnitTree root )
        {
            docTrees.printMessage( kind, msg, t, root );
        }

        public TypeMirror getLub( CatchTree tree )
        {
            return docTrees.getLub( tree );
        }
    }

    private class AsciidocComment implements Tokens.Comment
    {
        private final String asciidoc;
        private final Tokens.Comment comment;

        AsciidocComment( String asciidoc, Tokens.Comment comment )
        {
            this.asciidoc = asciidoc;
            this.comment = comment;
        }

        @Override
        public String getText()
        {
            return asciidoc;
        }

        @Override
        public int getSourcePos( int index )
        {
            // can we somehow map positions in the asciidoctor back to positions in the source javadoc?
            return 0;//comment.getSourcePos( index );
        }

        @Override
        public CommentStyle getStyle()
        {
            return comment.getStyle();
        }

        @Override
        public boolean isDeprecated()
        {
            return comment.isDeprecated();
        }
    }
}
