package org.asciidoctor.asciidoclet;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.javadoc.DocErrorReporter;
import org.asciidoctor.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Sets up a temporary directory containing output templates for use by Asciidoctor.
 */
class OutputTemplates {

    private final File templateDir;
    private final DocErrorReporter errorReporter;

    static final String[] templateNames = new String[] {
            "section.html.haml",
            "block_paragraph.html.haml"
    };

    OutputTemplates(DocErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        this.templateDir = prepareTemplateDir();
    }

    void addToOptions(OptionsBuilder optionsBuilder) {
        if (templateDir != null) optionsBuilder.templateDir(templateDir);
    }

    void delete() {
        if (templateDir != null) {
            for (String templateName : templateNames) new File(templateDir, templateName).delete();
            templateDir.delete();
        }
    }

    private File prepareTemplateDir() {
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

    private void prepareTemplate(File templateDir, String template) throws IOException {
        URL src = getClass().getClassLoader().getResource("templates/" + template);
        if (src == null) throw new IOException("Could not find template " + template);
        ByteSink dest = Files.asByteSink(new File(templateDir, template));
        Resources.asByteSource(src).copyTo(dest);
    }

}
