/*
 * Copyright 2013-2024 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import jdk.javadoc.doclet.Reporter;
import org.asciidoctor.*;
import org.asciidoctor.extension.RubyExtensionRegistry;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.asciidoctor.Asciidoctor.Factory.create;

/**
 * Doclet converter using and configuring AsciidoctorJ.
 *
 * @author John Ericksen
 */
class AsciidoctorConverter {

    static final String MARKER = " \t \t";

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

    private static final Pattern TYPE_PARAM = Pattern.compile("\\s*<(\\w+)>(.*)");
    private static final String INLINE_DOCTYPE = "inline";

    private final Asciidoctor asciidoctor;
    private final Optional<OutputTemplates> templates;
    private final Options options;

    AsciidoctorConverter(DocletOptions docletOptions, Reporter reporter) {
        this(docletOptions, reporter, OutputTemplates.create(reporter), create(docletOptions.gemPath()));
    }

    /**
     * Constructor used directly for testing purposes only.
     */
    AsciidoctorConverter(DocletOptions docletOptions, Reporter errorReporter, Optional<OutputTemplates> templates, Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.templates = templates;
        this.options = buildOptions(docletOptions, errorReporter);
    }

    private Options buildOptions(DocletOptions docletOptions, Reporter errorReporter) {
        final OptionsBuilder opts = defaultOptions();
        if (docletOptions.baseDir().isPresent()) {
            opts.baseDir(docletOptions.baseDir().get());
        }
        templates.ifPresent(outputTemplates -> opts.templateDir(outputTemplates.templateDir().toFile()));
        opts.attributes(buildAttributes(docletOptions, errorReporter));
        if (docletOptions.requires().size() > 0) {
            RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
            for (String require : docletOptions.requires()) {
                rubyExtensionRegistry.requireLibrary(require);
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
     * Converts a generic document (class, field, method, etc.).
     *
     * @param doc input
     */
    String convert(String doc) {
        if (doc.startsWith(MARKER)) {
            return doc;
        }
        final JavadocParser javadocParser = JavadocParser.parse(doc);

        final StringBuilder buffer = new StringBuilder(MARKER);
        String convert = convert(javadocParser.getCommentBody(), false);
        buffer.append(convert);
        buffer.append(System.lineSeparator());
        for (JavadocParser.Tag tag : javadocParser.tags()) {
            convertTag(tag, buffer);
            buffer.append(System.lineSeparator());
        }
        return buffer.toString();
    }

    void cleanup() throws IOException {
        if (templates.isPresent()) {
            templates.get().delete();
        }
    }

    /**
     * Renders a document tag in the standard way.
     *
     * @param tag    input
     * @param buffer output buffer
     */
    private void convertTag(JavadocParser.Tag tag, StringBuilder buffer) {
        buffer.append(tag.tagName).append(' ');

        // Special handling for @param <T> tags
        // See http://docs.oracle.com/javase/1.5.0/docs/tooldocs/windows/javadoc.html#@param
        if (tag.tagName.equals("@param")) {
            Matcher matcher = TYPE_PARAM.matcher(tag.tagText);
            if (matcher.find()) {
                buffer.append('<').append(matcher.group(1)).append('>');
                String text = matcher.group(2);
                if (!text.isBlank()) {
                    buffer.append(' ');
                }
                buffer.append(convert(text, true));
            } else {
                buffer.append(convert(tag.tagText, true));
            }
        } else {
            buffer.append(convert(tag.tagText, true));
        }
    }

    /**
     * Renders the input using Asciidoctor.
     * <p>
     * The source is first cleaned by stripping any trailing space after an
     * end line (e.g., `"\n "`), which gets left behind by the Javadoc
     * processor.
     *
     * @param input AsciiDoc source
     * @return content rendered by Asciidoctor
     */
    private String convert(String input, boolean inline) {
        if (input.trim().isEmpty()) {
            return "";
        }
        options.setDocType(inline ? INLINE_DOCTYPE : null);
        return asciidoctor.render(cleanJavadocInput(input), options);
    }

    static String cleanJavadocInput(String input) {
        return input.trim()
                .replaceAll("\n ", "\n") // Newline space to accommodate javadoc newlines.
                .replaceAll("\\{at}", "&#64;") // {at} is translated into @.
                .replaceAll("\\{slash}", "/") // {slash} is translated into /.
                .replaceAll("(?m)^( *)\\*\\\\/$", "$1*/"); // Multi-line comment end tag is translated into */.
    }
}
