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

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.tools.Diagnostic;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for copying the appropriate stylesheet to the javadoc
 * output directory.
 */
public class Stylesheets {
    
    static final String JAVA_STYLESHEET_FORMAT = "stylesheet%s.css";
    static final String JAVA11_STYLESHEET = String.format(JAVA_STYLESHEET_FORMAT, "11");

    private static final String CODERAY_STYLESHEET = "coderay-asciidoctor.css";
    private static final String OUTPUT_STYLESHEET = "stylesheet.css";

    private final Reporter errorReporter;

    Stylesheets(Reporter errorReporter) {
        this.errorReporter = errorReporter;
    }
    
    /**
     * Copies an {@link DocletEnvironment} to this object.
     * @param environment An environment to be copied to this object.
     * @return `true` if successfully copied. `false` otherwise.
     */
    public boolean copy(DocletEnvironment environment) {
        String stylesheet = selectStylesheet(System.getProperty("java.version"));
        errorReporter.print(Diagnostic.Kind.WARNING, "stylesheet=<"+ stylesheet + ">");
        JavaFileManager fm = environment.getJavaFileManager();
        try (InputStream stylesheetIn = getResource(stylesheet);
             InputStream coderayStylesheetIn = getResource(CODERAY_STYLESHEET);
             OutputStream stylesheetOut = openOutputStream(fm, OUTPUT_STYLESHEET);
             OutputStream coderayStylesheetOut = openOutputStream(fm, CODERAY_STYLESHEET)) {
            stylesheetIn.transferTo(stylesheetOut);
            coderayStylesheetIn.transferTo(coderayStylesheetOut);
            return true;
        } catch (IOException e) {
            errorReporter.print(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            return false;
        }
    }

    private OutputStream openOutputStream(JavaFileManager fm, String filename) throws IOException {
        return fm.getFileForOutput(DocumentationTool.Location.DOCUMENTATION_OUTPUT, "", filename, null).openOutputStream();
    }

    private InputStream getResource(String name) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Stylesheets.class.getClassLoader();
        }
        InputStream stream = loader.getResourceAsStream(name);
        if (stream != null) {
            return stream;
        }

        Module module = Stylesheets.class.getModule();
        if (module != null) {
            stream = module.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }

        throw new IllegalArgumentException("No such resource: " + name);
    }

    String selectStylesheet(String javaVersion) {
      String ret;
        Matcher m = Pattern.compile("^([0-9]+)(\\.)?.*").matcher(javaVersion);
        if (m.matches()) {
            int selectedJavaMajorVersion = 11;
            // It is safe to do parseInt since it is already ensured an integer parsable string by the regex
            int javaMajorVersionAsInt = Integer.parseInt(m.group(1));
            // In what version of Java, the stylesheet design was changed? 12, 13, 14?
            // The threshold 17 should be changed based on it.
            // Also, the filename stylesheet17.css should also be updated accordingly.
            if (11 <= javaMajorVersionAsInt  && javaMajorVersionAsInt < 17 )
                selectedJavaMajorVersion = 11;
            else if (17 <= javaMajorVersionAsInt)
                selectedJavaMajorVersion = 17;
            else
                errorReporter.print(Diagnostic.Kind.WARNING, "Unrecognized Java version " + javaVersion + ", using Java " + selectedJavaMajorVersion + " stylesheet");
            ret = String.format(JAVA_STYLESHEET_FORMAT, selectedJavaMajorVersion);
        } else {
           ret = JAVA11_STYLESHEET;
           errorReporter.print(Diagnostic.Kind.WARNING, "Unrecognizable Java version " + javaVersion + ", using Java 11 stylesheet");
        }
        return ret;
    }
}
