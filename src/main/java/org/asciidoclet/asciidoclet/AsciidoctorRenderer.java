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

import com.google.common.base.Optional;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import jdk.javadoc.doclet.Reporter;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import static org.asciidoctor.Asciidoctor.Factory.create;

/**
 * Doclet renderer using and configuring Asciidoctor.
 *
 * @author John Ericksen
 */
public class AsciidoctorRenderer {

    private static AttributesBuilder defaultAttributes() {
        return AttributesBuilder.attributes()
                .attribute("at", "&#64;")
                .attribute("slash", "/")
                .attribute("icons", null)
                .attribute("idprefix", "")
                .attribute("idseparator", "-")
                .attribute("javadoc", "")
                .attribute("showtitle", true)
                .attribute("source-highlighter", "coderay")
                .attribute("coderay-css", "class")
                .attribute("env-asciidoclet")
                .attribute("env", "asciidoclet");
    }

    private static OptionsBuilder defaultOptions() {
        return OptionsBuilder.options()
                .safe(SafeMode.SAFE)
                .backend("html5");
    }

    protected static final String INLINE_DOCTYPE = "inline";

    private final Asciidoctor asciidoctor;
    private final Optional<OutputTemplates> templates;
    private final Options options;

    public AsciidoctorRenderer(DocletOptions docletOptions, Reporter errorReporter) {
        this(docletOptions, errorReporter, OutputTemplates.create(errorReporter), create(docletOptions.gemPath()));
    }

    /**
     * Constructor used directly for testing purposes only.
     */
    protected AsciidoctorRenderer(DocletOptions docletOptions, Reporter errorReporter, Optional<OutputTemplates> templates, Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.templates = templates;
        this.options = buildOptions(docletOptions, errorReporter);
    }

    private Options buildOptions(DocletOptions docletOptions, Reporter errorReporter) {
        OptionsBuilder opts = defaultOptions();
        if (docletOptions.baseDir().isPresent()) {
            opts.baseDir(docletOptions.baseDir().get());
        }
        if (templates.isPresent()) {
            opts.templateDir(templates.get().templateDir());
        }
        opts.attributes(buildAttributes(docletOptions, errorReporter));
        if (docletOptions.requires().size() > 0) {
            for (String require : docletOptions.requires()) {
                asciidoctor.rubyExtensionRegistry().requireLibrary(require);
            }
        }
        return opts.get();
    }

    private Attributes buildAttributes(DocletOptions docletOptions, Reporter errorReporter) {
        return defaultAttributes()
                .attributes(new AttributesLoader(asciidoctor, docletOptions, errorReporter).load())
                .get();
    }

    /**
     * Renders a generic document (class, field, method, etc)
     *
     * @param doc input
     */
    public void renderDoc(Doc doc) {
        // hide text that looks like tags (such as annotations in source code) from Javadoc
        doc.setRawCommentText(doc.getRawCommentText().replaceAll("@([A-Z])", "{@literal @}$1"));

        StringBuilder buffer = new StringBuilder();
        buffer.append(render(doc.commentText(), false));
        buffer.append('\n');
        for (Tag tag : doc.tags()) {
            renderTag(tag, buffer);
            buffer.append('\n');
        }
        doc.setRawCommentText(buffer.toString());
    }

    public void cleanup() {
        if (templates.isPresent()) {
            templates.get().delete();
        }
    }

    /**
     * Renders a document tag in the standard way.
     *
     * @param tag input
     * @param buffer output buffer
     */
    private void renderTag(Tag tag, StringBuilder buffer) {
        buffer.append(tag.name()).append(' ');
        // Special handling for @param <T> tags
        // See http://docs.oracle.com/javase/1.5.0/docs/tooldocs/windows/javadoc.html#@param
        if ((tag instanceof ParamTag) && ((ParamTag) tag).isTypeParameter()) {
            ParamTag paramTag = (ParamTag) tag;
            buffer.append("<" + paramTag.parameterName() + ">");
            String text = paramTag.parameterComment();
            if (text.length() > 0) {
                buffer.append(' ').append(render(text, true));
            }
            return;
        }
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
    public String render(String input, boolean inline) {
        if (input.trim().isEmpty()) {
            return "";
        }
        options.setDocType(inline ? INLINE_DOCTYPE : null);
        return asciidoctor.render(cleanJavadocInput(input), options);
    }

    protected static String cleanJavadocInput(String input) {
        return input.trim()
            .replaceAll("\n ", "\n") // Newline space to accommodate javadoc newlines.
            .replaceAll("\\{at}", "&#64;") // {at} is translated into @.
            .replaceAll("\\{slash}", "/") // {slash} is translated into /.
            .replaceAll("(?m)^( *)\\*\\\\/$", "$1*/") // Multi-line comment end tag is translated into */.
            .replaceAll("\\{@literal (.*?)}", "$1"); // {@literal _} is translated into _ (standard javadoc).
    }
}
