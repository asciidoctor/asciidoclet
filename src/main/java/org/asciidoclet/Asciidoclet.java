package org.asciidoclet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;

/**
 * == Asciidoclet
 *
 * A Javadoc Doclet that uses http://asciidoctor.org[Asciidoctor] to
 * render http://asciidoc.org[AsciiDoc] markup in Javadoc comments.
 *
 * .*Examples:*
 *
 * Code block (with syntax highlighting added by CodeRay)::
 * +
 * [source,java]
 * ----
 * /**
 *  * == Asciidoclet
 *  *
 *  * A Javadoc Doclet that uses http://asciidoctor.org[Asciidoctor]
 *  * to render http://asciidoc.org[AsciiDoc] markup in Javadoc comments.
 *  *
 *  * {@literal @}author John Ericksen
 *  *{@literal /}
 * public class Asciidoclet extends Doclet {
 *     private final Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
 *
 *     public static boolean start(RootDoc rootDoc) {
 *         new Asciidoclet().render(rootDoc);  // <2>
 *         return Standard.start(rootDoc);
 *     }
 * }
 * ----
 * <1> Creates an instance of the Asciidoctor Java integration
 * <2> Runs Javadoc comment strings through Asciidoctor
 *
 * Inline code:: `code()` or +code()+
 *
 * Headings::
 * +
 * --
 * [float]
 * = Heading 1
 * [float]
 * == Heading 2
 * [float]
 * === Heading 3
 * [float]
 * ==== Heading 4
 * [float]
 * ===== Heading 5
 * --
 *
 * Links::
 * Doc Writer <doc@example.com> +
 * http://asciidoc.org[AsciiDoc] is a lightweight markup language. +
 * Learn more about it at http://asciidoctor.org. +
 *
 * Bullets::
 * +
 * --
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
 * --
 * +
 * --
 * .Numbered
 * . bullet
 * . bullet
 * .. bullet
 * .. bullet
 * . bullet
 * .. bullet
 * ... bullet
 * ... bullet
 * .... bullet
 * .... bullet
 * ... bullet
 * ... bullet
 * .. bullet
 * .. bullet
 * . bullet
 * --
 *
 * Tables::
 * +
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
 * Sidebar block::
 * +
 * .Optional Title
 * ****
 * *Sidebar* Block
 *
 * Usage: sidebar notes, naturally.
 * ****
 *
 * Admonitions::
 * +
 * IMPORTANT: Check this out!
 *
 * @version 0.1
 * @see org.asciidoclet.Asciidoclet
 * @since 0.1
 * @serial serial data
 * @author John Ericksen
 */
public class Asciidoclet extends Doclet {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private final AttributesBuilder attributesBuilder = AttributesBuilder.attributes()
        .attribute("icons", null).attribute("source-highlighter", "coderay")
        .attribute("coderay-css", "style");

    private final OptionsBuilder optionsBuilder = OptionsBuilder.options()
        .safe(SafeMode.SAFE).backend("html5").eruby("erubis");

    /**
     * .Example usage
     * [source,java]
     * exampleDeprecated("do not use");
     *
     * @deprecated for example purposes
     * @exception Exception example
     * @throws RuntimeException example
     * @serialData something else
     * @link Asciidoclet
     */
    public static void exampleDeprecated(String field) throws Exception{
        //noop
    }

    /**
     * Sets the language version to Java 5.
     *
     * _Javadoc spec requirement._
     *
     * @return language version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * Sets the option length to the standard Javadoc option length.
     *
     * _Javadoc spec requirement._
     *
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        return Standard.optionLength(option);
    }

    /**
     * The starting point of Javadoc render.
     *
     * _Javadoc spec requirement._
     *
     * @param rootDoc input class documents
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        new Asciidoclet().render(rootDoc);

        return Standard.start(rootDoc);
    }

    /**
     * Processes the input options by delegating to the standard handler.
     *
     * _Javadoc spec requirement._
     *
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
     *
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
     *
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
     *
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
     * Renders a document tag in the standard way.
     *
     * @param tag input
     * @param buffer output buffer
     */
    private void renderTag(Tag tag, StringBuilder buffer) {
        //print out directly
        buffer.append(tag.name());
        buffer.append(" ");
        //buffer.append(render(tag.text()));
        // FIXME render tag text isn't work atm
        buffer.append(tag.text());
    }

    /**
     * Renders the input using Asciidoctor.
     *
     * The source is first cleaned by stripping any trailing space after an
     * end line (e.g., `"\n "`), which gets left behind by the Javadoc
     * processor.
     *
     * @param input AsciiDoc source
     * @return content rendered by Asciidoctor
     */
    private String render(String input) {
        // Replace "\n " to remove default Javadoc space.
        String cleanedInput = input.trim().replaceAll("\n ", "\n")
            .replaceAll("\\{@literal (.*?)}", "$1");
        Map<String, Object> options = optionsBuilder.attributes(attributesBuilder.asMap()).asMap();
        return asciidoctor.render(cleanedInput, options);
    }
}
