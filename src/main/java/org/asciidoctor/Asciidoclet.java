package org.asciidoctor;

import com.sun.javadoc.*;
import com.sun.tools.doclets.standard.Standard;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * = Asciidoclet
 * 
 * https://github.com/asciidoctor/asciidoclet[Asciidoclet] is a Javadoc Doclet
 * that uses http://asciidoctor.org[Asciidoctor] (via the
 * https://github.com/asciidoctor/asciidoctor-java-integration[Asciidoctor Java integration])
 * to render http://asciidoc.org[AsciiDoc] markup within Javadoc comments.
 *
 * == Usage
 * 
 * Asciidoclet may be used via a custom doclet in the maven-javadoc-plugin:
 *
 * [source,xml]
 * <plugin>
 *   <groupId>org.apache.maven.plugins</groupId>
 *   <artifactId>maven-javadoc-plugin</artifactId>
 *   <version>2.9</version>
 *   <configuration>
 *     <source>1.7</source>
 *     <doclet>org.asciidoctor.Asciidoclet</doclet>
 *     <docletArtifact>
 *       <groupId>org.asciidoclet</groupId>
 *       <artifactId>asciidoclet</artifactId>
 *       <version>${asciidoclet.version}</version>
 *     </docletArtifact>
 *   </configuration>
 * </plugin>
 *
 * == Examples
 * 
 * Code block (with syntax highlighting added by CodeRay)::
 * +
 * [source,java]
 * --
 * /**
 *  * = Asciidoclet
 *  *
 *  * A Javadoc Doclet that uses http://asciidoctor.org[Asciidoctor]
 *  * to render http://asciidoc.org[AsciiDoc] markup in Javadoc comments.
 *  *
 *  * @author https://github.com/johncarl81[John Ericksen]
 *  *\/
 * public class Asciidoclet extends Doclet {
 *     private final Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
 *
 *     @SuppressWarnings("UnusedDeclaration")
 *     public static boolean start(RootDoc rootDoc) {
 *         new Asciidoclet().render(rootDoc); // <2>
 *         return Standard.start(rootDoc);
 *     }
 * }
 * --
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
 * .Unnumbered
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
 * [cols="3", options="header"]
 * |===
 * |Column 1
 * |Column 2
 * |Column 3
 * 
 * |1
 * |Item 1
 * |a
 * 
 * |2
 * |Item 2
 * |b
 * 
 * |3
 * |Item 3
 * |c
 * |===
 *
 * Sidebar block::
 * +
 * .Optional Title
 * ****
 * *Sidebar* Block
 *
 * Usage: Notes in a sidebar, naturally.
 * ****
 *
 * Admonitions::
 * +
 * IMPORTANT: Check this out!
 *
 * @author https://github.com/johncarl81[John Ericksen]
 * @version 0.1.0
 * @see org.asciidoctor.Asciidoclet
 * @since 0.1.0
 * @serial (or @serialField or @serialData)
 */
public class Asciidoclet extends Doclet {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private final AttributesBuilder attributesBuilder = AttributesBuilder.attributes()
        .attribute("at", "&#64;")
        .attribute("slash", "/")
        .attribute("icons", null)
        .attribute("idprefix", "")
        .attribute("notitle", null)
        .attribute("source-highlighter", "coderay")
        .attribute("coderay-css", "style");

    private final OptionsBuilder optionsBuilder = OptionsBuilder.options()
        .safe(SafeMode.SAFE).backend("html5").eruby("erubis");

    private final Pattern inlineContentRe = Pattern.compile("(?s).*?<p>\\s*(.*?)\\s*</p>.*");
    
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
        // hide text that looks like tags (such as annotations in source code) from Javadoc
        // replacing the PackageDoc raw comment text issues a warning, perhaps in the case it's empty
        if (!(doc instanceof PackageDoc)) {
            doc.setRawCommentText(doc.getRawCommentText().replaceAll("@([A-Z])", "{@literal @}$1"));
        }
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
        // switch to doctype=inline to render tag text once Asciidoctor #328 is resolved
        String renderedText = render(tag.text());
        Matcher inlineContentMatcher = inlineContentRe.matcher(renderedText);
        if (inlineContentMatcher.matches()) {
        	buffer.append(inlineContentMatcher.group(1));
        }
        else {
        	buffer.append(tag.text());
        }
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
        String cleanedInput = input.trim().replaceAll("\n ", "\n").replaceAll("\\{at}", "&#64;").replaceAll("\\{slash}", "/")
            .replaceAll("(?m)^( *)\\*\\\\/$", "$1*/").replaceAll("\\{@literal (.*?)}", "$1");
        Map<String, Object> options = optionsBuilder.attributes(attributesBuilder.asMap()).asMap();
        return asciidoctor.render(cleanedInput, options);
    }
}
