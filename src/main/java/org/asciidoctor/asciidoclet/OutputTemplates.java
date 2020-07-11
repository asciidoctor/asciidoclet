/**
 * Copyright 2013-2015 John Ericksen
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
package org.asciidoctor.asciidoclet;

import com.sun.javadoc.DocErrorReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Sets up a temporary directory containing output templates for use by Asciidoctor.
 */
public class OutputTemplates {

    private static final String[] TEMPLATE_NAMES = new String[] {
            "section.html.haml",
            "paragraph.html.haml"
    };

    private final File templateDir;

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
        for (String templateName : TEMPLATE_NAMES) {
            new File(templateDir, templateName).delete();
        }
        templateDir.delete();
    }

    private static File prepareTemplateDir(DocErrorReporter errorReporter) {
        // copy our template resources to the templateDir so Asciidoctor can use them.
        File templateDir = Resources.createTempDir();
        try {
            for (String templateName : TEMPLATE_NAMES) {
                prepareTemplate(templateDir, templateName);
            }
            return templateDir;
        } catch (IOException e) {
            errorReporter.printWarning("Failed to prepare templates: " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void prepareTemplate(File templateDir, String template) throws IOException {
        InputStream src = OutputTemplates.class.getClassLoader().getResourceAsStream("templates/" + template);
        if (src == null) {
            throw new IOException("Could not find template " + template);
        }
        OutputStream dest = new FileOutputStream(new File(templateDir, template));
        Resources.copy(src, dest);
    }

}
