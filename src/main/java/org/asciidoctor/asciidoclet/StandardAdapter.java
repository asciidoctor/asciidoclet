package org.asciidoctor.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

/**
 * Adapter class to use the Standard Javadoc Doclet in a non-static context.
 *
 * @author John Ericksen
 */
public class StandardAdapter {

    public int optionLength(String option) {
        return Standard.optionLength(option);
    }

    public boolean start(RootDoc rootDoc) {
        return Standard.start(rootDoc);
    }

    public boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return Standard.validOptions(options, errorReporter);
    }
}
