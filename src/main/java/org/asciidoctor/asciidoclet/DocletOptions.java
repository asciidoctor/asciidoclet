package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Provides an interface to the doclet options we are interested in.
 */
public class DocletOptions {
    public static final String ENCODING = "-encoding";
    public static final String OVERVIEW = "-overview";
    public static final String INCLUDE_BASEDIR = "-include-basedir";
    public static final String STYLESHEETFILE = "-stylesheetfile";
    public static final String DESTDIR = "-d";

    private final Optional<File> basedir;
    private final Optional<File> overview;
    private final Optional<File> stylesheet;
    private final Optional<File> destdir;
    private final Charset encoding;

    public static final DocletOptions NONE = new DocletOptions(new String[][]{});

    public DocletOptions(RootDoc rootDoc) {
        this(rootDoc.options());
    }

    public DocletOptions(String[][] options) {
        File basedir = null;
        File overview = null;
        File stylesheet = null;
        File destdir = null;
        Charset encoding = Charset.defaultCharset();
        for (String[] option : options) {
            if (option.length > 0) {
                if (INCLUDE_BASEDIR.equals(option[0])) {
                    basedir = new File(option[1]);
                }
                else if (OVERVIEW.equals(option[0])) {
                    overview = new File(option[1]);
                }
                else if (STYLESHEETFILE.equals(option[0])) {
                    stylesheet = new File(option[1]);
                }
                else if (DESTDIR.equals(option[0])) {
                    destdir = new File(option[1]);
                }
                else if (ENCODING.equals(option[0])) {
                    encoding = Charset.forName(option[1]);
                }
            }
        }

        this.basedir = Optional.fromNullable(basedir);
        this.overview = Optional.fromNullable(overview);
        this.stylesheet = Optional.fromNullable(stylesheet);
        this.destdir = Optional.fromNullable(destdir);
        this.encoding = encoding;
    }

    public Optional<File> overview() {
        return overview;
    }

    public Optional<File> stylesheetFile() {
        return stylesheet;
    }

    public Optional<File> includeBasedir() {
        return basedir;
    }

    public Optional<File> destDir() {
        return destdir;
    }

    public Charset encoding() {
        return encoding;
    }

    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter, StandardAdapter standardDoclet) {
        DocletOptions docletOptions = new DocletOptions(options);

        if (!docletOptions.includeBasedir().isPresent()) {
            errorReporter.printWarning(INCLUDE_BASEDIR + " must be present for includes or file reference features.");
        }

        return standardDoclet.validOptions(options, errorReporter);
    }

    public static int optionLength(String option, StandardAdapter standardDoclet) {
        if (INCLUDE_BASEDIR.equals(option)) {
            return 2;
        }
        return standardDoclet.optionLength(option);
    }

}
