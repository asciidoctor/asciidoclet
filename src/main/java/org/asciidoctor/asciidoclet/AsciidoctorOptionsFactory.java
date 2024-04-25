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
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.extension.RubyExtensionRegistry;

/**
 * Asciidoctor Options Factory.
 *
 * @since 2.0.0
 */
class AsciidoctorOptionsFactory {

    private static final String DEFAULT_BACKEND = "html5";

    private final Asciidoctor asciidoctor;
    private final Reporter reporter;

    AsciidoctorOptionsFactory(Asciidoctor asciidoctor, Reporter reporter) {
        this.asciidoctor = asciidoctor;
        this.reporter = reporter;
    }

    Options create(DocletOptions docletOptions, OutputTemplates templates) {
        final OptionsBuilder opts = defaultOptions();
        if (docletOptions.baseDir().isPresent()) {
            opts.baseDir(docletOptions.baseDir().get());
        }
        if (templates != null) {
            opts.templateDir(templates.templateDir().toFile());
        }

        opts.attributes(buildAttributes(docletOptions));
        if (docletOptions.requires().size() > 0) {
            RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
            for (String require : docletOptions.requires()) {
                rubyExtensionRegistry.requireLibrary(require);
            }
        }
        return opts.get();
    }

    private Attributes buildAttributes(DocletOptions docletOptions) {
        return defaultAttributes()
                .attributes(new AttributesLoader(asciidoctor, docletOptions, reporter).load())
                .get();
    }

    private static OptionsBuilder defaultOptions() {
        return Options.builder()
                .safe(SafeMode.SAFE)
                .backend(DEFAULT_BACKEND);
    }

    private static AttributesBuilder defaultAttributes() {
        return org.asciidoctor.Attributes.builder()
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

}
