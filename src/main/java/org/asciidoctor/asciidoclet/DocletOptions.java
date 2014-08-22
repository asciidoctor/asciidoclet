package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Provides an interface to the doclet options we are interested in.
 */
public class DocletOptions {
    public static final String ENCODING = "-encoding";
    public static final String OVERVIEW = "-overview";
    public static final String BASEDIR = "--base-dir";
    public static final String STYLESHEET = "-stylesheetfile";
    public static final String DESTDIR = "-d";
    public static final String ATTRIBUTE = "-a";
    public static final String ATTRIBUTE_LONG = "--attribute";
    public static final String ATTRIBUTES_FILE = "--attributes-file";
    public static final String GEM_PATH = "--gem-path";
    public static final String REQUIRE = "-r";
    public static final String REQUIRE_LONG = "--require";

    private final Optional<File> basedir;
    private final Optional<File> overview;
    private final Optional<File> stylesheet;
    private final Optional<File> destdir;
    private final Optional<File> attributesFile;
    private final String gemPath;
    private final List<String> requires;
    private final Charset encoding;
    private final List<String> attributes;

    public static final DocletOptions NONE = new DocletOptions(new String[][]{});

    public DocletOptions(RootDoc rootDoc) {
        this(rootDoc.options());
    }

    public DocletOptions(String[][] options) {
        File basedir = null;
        File overview = null;
        File stylesheet = null;
        File destdir = null;
        File attrsFile = null;
        String gemPath = null;
        ImmutableList.Builder<String> requires = ImmutableList.builder();
        Charset encoding = Charset.defaultCharset();
        ImmutableList.Builder<String> attrs = ImmutableList.builder();
        for (String[] option : options) {
            if (option.length > 0) {
                if (BASEDIR.equals(option[0])) {
                    basedir = new File(option[1]);
                }
                else if (OVERVIEW.equals(option[0])) {
                    overview = new File(option[1]);
                }
                else if (STYLESHEET.equals(option[0])) {
                    stylesheet = new File(option[1]);
                }
                else if (DESTDIR.equals(option[0])) {
                    destdir = new File(option[1]);
                }
                else if (ENCODING.equals(option[0])) {
                    encoding = Charset.forName(option[1]);
                }
                else if (ATTRIBUTE.equals(option[0]) || ATTRIBUTE_LONG.equals(option[0])) {
                    attrs.addAll(COMMA_WS.split(option[1]));
                }
                else if (ATTRIBUTES_FILE.equals(option[0])) {
                    attrsFile = new File(option[1]);
                }
                else if (GEM_PATH.equals(option[0])) {
                    gemPath = option[1];
                }
                else if (REQUIRE.equals(option[0]) || REQUIRE_LONG.equals(option[0])) {
                    requires.addAll(COMMA_WS.split(option[1]));
                }
            }
        }

        this.basedir = Optional.fromNullable(basedir);
        this.overview = Optional.fromNullable(overview);
        this.stylesheet = Optional.fromNullable(stylesheet);
        this.destdir = Optional.fromNullable(destdir);
        this.encoding = encoding;
        this.attributes = attrs.build();
        this.attributesFile = Optional.fromNullable(attrsFile);
        this.gemPath = gemPath;
        this.requires = requires.build();
    }

    public Optional<File> overview() {
        return overview;
    }

    public Optional<File> stylesheet() {
        return stylesheet;
    }

    public Optional<File> baseDir() {
        return basedir;
    }

    public Optional<File> destDir() {
        return destdir;
    }

    public Charset encoding() {
        return encoding;
    }

    public List<String> attributes() {
        return attributes;
    }

    Optional<File> attributesFile() {
        if (!attributesFile.isPresent()) return attributesFile;
        File f = attributesFile.get();
        if (!f.isAbsolute() && basedir.isPresent()) {
            f = new File(basedir.get(), f.getPath());
        }
        return Optional.of(f);
    }

    public String gemPath() {
        return gemPath;
    }

    public List<String> requires() {
        return requires;
    }

    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter, StandardAdapter standardDoclet) {
        DocletOptions docletOptions = new DocletOptions(options);

        if (!docletOptions.baseDir().isPresent()) {
            errorReporter.printWarning(BASEDIR + " must be present for includes or file reference features to work properly.");
        }

        Optional<File> attrsFile = docletOptions.attributesFile();
        if (attrsFile.isPresent() && !attrsFile.get().canRead()) {
            errorReporter.printWarning("Cannot read attributes file " + attrsFile.get());
        }

        return standardDoclet.validOptions(options, errorReporter);
    }

    public static int optionLength(String option, StandardAdapter standardDoclet) {
        if (BASEDIR.equals(option)) {
            return 2;
        }
        if (ATTRIBUTE.equals(option) || ATTRIBUTE_LONG.equals(option)) {
            return 2;
        }
        if (ATTRIBUTES_FILE.equals(option)) {
            return 2;
        }
        if (GEM_PATH.equals(option)) {
            return 2;
        }
        if (REQUIRE.equals(option) || REQUIRE_LONG.equals(option)) {
            return 2;
        }
        return standardDoclet.optionLength(option);
    }

    // Split on comma with optional whitespace
    private static final Splitter COMMA_WS = Splitter.onPattern("\\s*,\\s*").omitEmptyStrings().trimResults();
}
