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
    static String JAVA8_STYLESHEET = "stylesheet8.css";
    static String JAVA6_STYLESHEET = "stylesheet6.css";
    static String CODERAY_STYLESHEET = "coderay-asciidoctor.css";
    static String OUTPUT_STYLESHEET = "stylesheet.css";

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
        File destFile = new File(docletOptions.destDir().get(), OUTPUT_STYLESHEET);
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
        if (javaVersion.matches("^1\\.[56]\\D.*")) return JAVA6_STYLESHEET;
        if (javaVersion.matches("^1\\.[78]\\D.*")) return JAVA8_STYLESHEET;
        errorReporter.printWarning("Unrecognized Java version " + javaVersion + ", using Java 7/8 stylesheet");
        // TODO: review this when Java 9 becomes available!
        return JAVA8_STYLESHEET;
    }
}
