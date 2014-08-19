package org.asciidoctor.asciidoclet;

import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Tag;
import org.asciidoctor.*;

import java.io.File;

import static org.asciidoctor.Asciidoctor.Factory.create;

/**
 * Doclet renderer using and configuring Asciidoctor.
 *
 * @author John Ericksen
 */
public class AsciidoctorRenderer implements DocletRenderer {

    private static final Attributes ATTRIBUTES = AttributesBuilder.attributes()
            .attribute("at", "&#64;")
            .attribute("slash", "/")
            .attribute("icons", null)
            .attribute("idprefix", "")
            .attribute("showtitle", true)
            .attribute("source-highlighter", "coderay")
            .attribute("coderay-css", "style").get();
    protected static final String INLINE_DOCTYPE = "inline";

    private final Asciidoctor asciidoctor;
    private final String baseDir;
    private final OutputTemplates templates;

    public AsciidoctorRenderer(String baseDir, DocErrorReporter errorReporter) {
        this(baseDir, new OutputTemplates(errorReporter), create());
    }

    /**
     * Constructor used directly for testing purposes only.
     *
     * @param baseDir
     * @param asciidoctor
     */
    protected AsciidoctorRenderer(String baseDir, OutputTemplates templates, Asciidoctor asciidoctor) {
        this.baseDir = baseDir;
        this.asciidoctor = asciidoctor;
        this.templates = templates;
    }

    /**
     * Renders a generic document (class, field, method, etc)
     *
     * @param doc input
     */
    @Override
    public void renderDoc(Doc doc) {
        // hide text that looks like tags (such as annotations in source code) from Javadoc
        doc.setRawCommentText(doc.getRawCommentText().replaceAll("@([A-Z])", "{@literal @}$1"));

        StringBuilder buffer = new StringBuilder();
        buffer.append(render(doc.commentText(), false));
        buffer.append('\n');
        for ( Tag tag : doc.tags() ) {
            renderTag(tag, buffer);
            buffer.append('\n');
        }
        doc.setRawCommentText(buffer.toString());
    }

    public void cleanup() {
        templates.delete();
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
        buffer.append(render(tag.text(), true));
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
    private String render(String input, boolean inline) {

        OptionsBuilder optionsBuilder = OptionsBuilder.options()
                .safe(SafeMode.SAFE)
                .backend("html5")
                .attributes(ATTRIBUTES);

        if(this.baseDir != null){
            optionsBuilder.baseDir(new File(this.baseDir));
        }
        if(inline){
            optionsBuilder.docType(INLINE_DOCTYPE);
        }
        templates.addToOptions(optionsBuilder);

        return asciidoctor.render(cleanJavadocInput(input), optionsBuilder.get());
    }

    protected String cleanJavadocInput(String input){
        return input.trim()
            .replaceAll("\n ", "\n") // Newline space to accommodate javadoc newlines.
            .replaceAll("\\{at}", "&#64;") // {at} is translated into @.
            .replaceAll("\\{slash}", "/") // {slash} is translated into /.
            .replaceAll("(?m)^( *)\\*\\\\/$", "$1*/") // Multi-line comment end tag is translated into */.
            .replaceAll("\\{@literal (.*?)}", "$1"); // {@literal _} is translated into _ (standard javadoc).
    }
}
