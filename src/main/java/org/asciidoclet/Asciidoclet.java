package org.asciidoclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.standard.Standard;
import org.asciidoctor.Asciidoctor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Asciidoclet
 * -----------
 * A Javadoc Doclet that uses Asciidoc for rendering javadoc comments.
 *
 * *Examples:*
 *
 * Code:
 * [code,java]
 * ----
 * /**
 *  * Asciidoclet comments
 *  *
 *  *{@literal /}
 * public class Asciidoclet{
 *     public Asciidoclet(){}
 * }
 * ----
 *
 * inline code: `code()`
 *
 * = Heading 1
 * == Heading 2
 * === Heading 3
 * ==== Heading 4
 *
 * Doc Writer <doc@example.com>
 *
 * An introduction to http://asciidoc.org[AsciiDoc].
 *
 * .Bulleted
 * * bullet
 * * bullet
 * - bullet
 * - bullet
 * * bullet
 * ** bullet
 * ** bullet
 * *** bullet
 * *** bullet
 * **** bullet
 * **** bullet
 * ***** bullet
 * ***** bullet
 * **** bullet
 * *** bullet
 * ** bullet
 * * bullet
 *
 *
 * .An example table
 * [options="header,footer"]
 * |=======================
 * |Col 1|Col 2      |Col 3
 * |1    |Item 1     |a
 * |2    |Item 2     |b
 * |3    |Item 3     |c
 * |6    |Three items|d
 * |=======================
 *
 * .Optional Title
 * ****
 * *Sidebar* Block
 *
 * Use: sidebar notes :)
 * ****
 *
 * IMPORTANT: Important.
 *
 * @author John Ericksen
 * @version 0.1
 * @see org.asciidoclet.Asciidoclet
 * @since 0.1
 * @serial (or @serialField or @serialData)
 * @author John Ericksen
 */
public class Asciidoclet extends Doclet  {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    /**
     * Example usage:
     * [code,java]
     * ----
     * exampleDepreciated("do not use");
     * ----
     *
     * @deprecated for example purposes
     * @exception Exception example
     * @throws RuntimeException example
     * @serialData something else
     * @link Asciidoclet
     */
    public static void exampleDepreciated(String field) throws Exception{
        //noop
    }

    /**
     * Javadoc spec requirement.
     * @return language version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * Javadoc spec requirement.
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        return Standard.optionLength(option);
    }

    /**
     * Javadoc spec requirement.  Starting point of Javadoc render.
     * @param rootDoc input class documents
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        new Asciidoclet().render(rootDoc);

        return Standard.start(rootDoc);
    }

    /**
     * Javadoc spec requirement.  Handles the input options.
     * @param options input option array
     * @param errorReporter error handling
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return Standard.validOptions(options, errorReporter);
    }

    /**
     * Renders the input document.
     * @param rootDoc input
     */
    private void render(RootDoc rootDoc) {
        Set<PackageDoc> packages = new HashSet<PackageDoc>();
        for ( ClassDoc doc : rootDoc.classes() ) {
            packages.add(doc.containingPackage());
            renderClass(doc);
        }
        for ( PackageDoc doc : packages ) {
            renderDoc(doc);
        }
    }

    /**
     * Renders an individual class.
     * @param doc input
     */
    private void renderClass(ClassDoc doc) {
        //handle the various parts of the Class doc
        renderDoc(doc);
        for ( MemberDoc member : doc.fields() ) {
            renderDoc(member);
        }
        for ( MemberDoc member : doc.constructors() ) {
            renderDoc(member);
        }
        for ( MemberDoc member : doc.methods() ) {
            renderDoc(member);
        }
        if ( doc instanceof AnnotationTypeDoc ) {
            for ( MemberDoc member : ((AnnotationTypeDoc)doc).elements() ) {
                renderDoc(member);
            }
        }
    }

    /**
     * Renders a generic document (class, field, method, etc)
     * @param doc input
     */
    private void renderDoc(Doc doc) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(render(doc.commentText()));
        buffer.append('\n');
        for ( Tag tag : doc.tags() ) {
            renderTag(tag, buffer);
            buffer.append('\n');
        }
        doc.setRawCommentText(buffer.toString());
    }

    /**
     * Renders a document tag
     * @param tag input
     * @param buffer output buffer
     */
    private void renderTag(Tag tag, StringBuilder buffer) {
        //print out directly
        buffer.append(tag.name());
        buffer.append(" ");
        buffer.append(render(tag.text()));
    }

    private String render(String input){
        // Replace "\n " to remove default javadoc space.
        String reworked = input.trim().replaceAll("\n ", "\n");
        return asciidoctor.render(reworked, Collections.<String, Object>emptyMap());
    }
}
