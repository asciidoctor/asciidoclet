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

import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sets up a temporary directory containing output templates for use by Asciidoctor.
 */
class OutputTemplates {

    private static final String[] TEMPLATE_NAMES = new String[]{"section.html.haml", "paragraph.html.haml"};

    private final Path templateDir;

    private OutputTemplates(Path templateDir) {
        this.templateDir = templateDir;
    }

    static OutputTemplates create(Reporter reporter) {
        final Path dir = prepareTemplateDir(reporter);
        return dir != null ? new OutputTemplates(dir) : null;
    }

    Path templateDir() {
        return templateDir;
    }

    /**
     * Creates and returns a directory in OS's temps path holding Asciidoctor
     * templates during conversion.
     *
     * @param reporter doclet {@link Reporter}.
     * @return Path to templates, or null if it could not be created.
     */
    private static Path prepareTemplateDir(Reporter reporter) {
        // copy our template resources to the templateDir so Asciidoctor can use them.
        try {
            final Path templateDir = Files.createTempDirectory("asciidoclet");
            for (String templateName : TEMPLATE_NAMES) {
                prepareTemplate(templateDir, templateName);
            }
            return templateDir;
        } catch (IOException e) {
            reporter.print(Diagnostic.Kind.WARNING, "Failed to prepare templates: " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Copies Asciidoctor templates into a temporal location.
     * First, attempts to locate templates in the Java module, then attempts
     * direct Classpath search. This is to ensure it works in both test, and
     * shaded JAR.
     *
     * @param templateDir path where to copy the templates
     * @param template    Asciidoctor template name
     */
    private static void prepareTemplate(Path templateDir, String template) throws IOException {
        final String templatePath = "templates/" + template;
        final InputStream input = ModuleLayer.boot()
                .findModule("asciidoclet")
                .map(module -> getResourceAsStream(module, templatePath))
                .orElseGet(() -> OutputTemplates.class.getClassLoader().getResourceAsStream(templatePath));

        if (input == null) {
            throw new IOException("Could not find template " + template);
        }
        final Path path = templateDir.resolve(template);
        try (OutputStream output = Files.newOutputStream(path)) {
            input.transferTo(output);
        } finally {
            input.close();
        }
    }

    private static InputStream getResourceAsStream(Module module, String path) {
        try {
            return module.getResourceAsStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
