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

import com.google.common.io.Resources;
import com.sun.javadoc.DocErrorReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for copying the appropriate stylesheet to the javadoc
 * output directory.
 */
public class Stylesheets {
    static final String JAVA9_STYLESHEET = "stylesheet9.css";
    static final String JAVA8_STYLESHEET = "stylesheet8.css";
    static final String JAVA6_STYLESHEET = "stylesheet6.css";
    static final String CODERAY_STYLESHEET = "coderay-asciidoctor.css";
    static final String OUTPUT_STYLESHEET = "stylesheet.css";

    private final DocletOptions docletOptions;
    private final DocErrorReporter errorReporter;

    public Stylesheets(DocletOptions options, DocErrorReporter errorReporter) {
        this.docletOptions = options;
        this.errorReporter = errorReporter;
    }

    public boolean copy() {
        if (!docletOptions.destDir().isPresent()) {
            // standard doclet must have checked this by the time we are called
            errorReporter.printError("Destination directory not specified, cannot copy stylesheet");
            return false;
        }
        String stylesheet = selectStylesheet(System.getProperty("java.version"));
        File destDir = docletOptions.destDir().get();
        try {
            Resources.copy(Resources.getResource(stylesheet), new FileOutputStream(new File(destDir, OUTPUT_STYLESHEET)));
            Resources.copy(Resources.getResource(CODERAY_STYLESHEET), new FileOutputStream(new File(destDir, CODERAY_STYLESHEET)));
            return true;
        } catch (IOException e) {
            errorReporter.printError(e.getLocalizedMessage());
            return false;
        }
    }

    String selectStylesheet(String javaVersion) {
        if (javaVersion.matches("^1\\.[56]\\D.*")) {
            return JAVA6_STYLESHEET;
        }
        if (javaVersion.matches("^1\\.[78]\\D.*")) {
            return JAVA8_STYLESHEET;
        }
        if (javaVersion.matches("^(9|10)(\\.)?.*")) {
            return JAVA9_STYLESHEET;
        }
        errorReporter.printWarning("Unrecognized Java version " + javaVersion + ", using Java 9 stylesheet");
        // TODO: review this when Java 11 becomes available and/or make more configurable!
        return JAVA9_STYLESHEET;
    }
}
