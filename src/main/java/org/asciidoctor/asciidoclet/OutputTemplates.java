package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.javadoc.DocErrorReporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Sets up a temporary directory containing output templates for use by Asciidoctor.
 */
class OutputTemplates {

    private final File templateDir;

    static final String[] templateNames = new String[] {
            "section.html.haml",
            "block_paragraph.html.haml"
    };

    private OutputTemplates(File templateDir) {
        this.templateDir = templateDir;
    }

    static Optional<OutputTemplates> create(DocErrorReporter errorReporter) {
        File dir = prepareTemplateDir(errorReporter);
        return dir == null ? Optional.<OutputTemplates>absent() : Optional.of(new OutputTemplates(dir));
    }

    File templateDir() {
        return templateDir;
    }

    void delete() {
        for (String templateName : templateNames) new File(templateDir, templateName).delete();
        templateDir.delete();
    }

    private static File prepareTemplateDir(DocErrorReporter errorReporter) {
        // copy our template resources to the templateDir so Asciidoctor can use them.
        File templateDir = Files.createTempDir();
        try {
            for (String templateName : templateNames) prepareTemplate(templateDir, templateName);
            return templateDir;
        } catch (IOException e) {
            errorReporter.printWarning("Failed to prepare templates: " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void prepareTemplate(File templateDir, String template) throws IOException {
        URL src = OutputTemplates.class.getClassLoader().getResource("templates/" + template);
        if (src == null) throw new IOException("Could not find template " + template);
        ByteSink dest = Files.asByteSink(new File(templateDir, template));
        Resources.asByteSource(src).copyTo(dest);
    }

}
