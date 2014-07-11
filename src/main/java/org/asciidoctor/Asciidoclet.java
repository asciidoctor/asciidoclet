package org.asciidoctor;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.asciidoctor.asciidoclet.*;

/**
 * = Asciidoclet
 *
 * https://github.com/asciidoctor/asciidoclet[Asciidoclet] is a Javadoc Doclet
 * that uses http://asciidoctor.org[Asciidoctor] (via the
 * https://github.com/asciidoctor/asciidoctor-java-integration[Asciidoctor Java integration])
 * to render http://asciidoc.org[AsciiDoc] markup within Javadoc comments.
 *
 * == Usage
 * 
 * Asciidoclet may be used via a custom doclet in the maven-javadoc-plugin:
 *
 * [source,xml]
 * ----
 * include::pom.xml[tags=pom_include]
 * ----
 *
 * <1> The -includes-basedir option must be set, typically this is the project root. It allows
 * source inclusions within javadocs, relative to the specified directory.
 *
 * == Examples
 * 
 * Code block (with syntax highlighting added by CodeRay)::
 * +
 * [source,java]
 * --
 * /**
 *  * = Asciidoclet
 *  *
 *  * A Javadoc Doclet that uses http://asciidoctor.org[Asciidoctor]
 *  * to render http://asciidoc.org[AsciiDoc] markup in Javadoc comments.
 *  *
 *  * @author https://github.com/johncarl81[John Ericksen]
 *  *\/
 * public class Asciidoclet extends Doclet {
 *     private final Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
 *
 *     @SuppressWarnings("UnusedDeclaration")
 *     public static boolean start(RootDoc rootDoc) {
 *         new Asciidoclet().render(rootDoc); // <2>
 *         return Standard.start(rootDoc);
 *     }
 * }
 * --
 * <1> Creates an instance of the Asciidoctor Java integration
 * <2> Runs Javadoc comment strings through Asciidoctor
 *
 * Inline code:: `code()` or +code()+
 *
 * Headings::
 * +
 * --
 * [float]
 * = Heading 1
 * [float]
 * == Heading 2
 * [float]
 * === Heading 3
 * [float]
 * ==== Heading 4
 * [float]
 * ===== Heading 5
 * --
 *
 * Links::
 * Doc Writer <doc@example.com> +
 * http://asciidoc.org[AsciiDoc] is a lightweight markup language. +
 * Learn more about it at http://asciidoctor.org. +
 *
 * Bullets::
 * +
 * --
 * .Unnumbered
 * * bullet
 * * bullet
 * - bullet
 * - bullet
 * * bullet
 * ** bullet
 * ** bullet
 * *** bullet
 * *** bullet
 * **** bullet
 * **** bullet
 * ***** bullet
 * ***** bullet
 * **** bullet
 * *** bullet
 * ** bullet
 * * bullet
 * --
 * +
 * --
 * .Numbered
 * . bullet
 * . bullet
 * .. bullet
 * .. bullet
 * . bullet
 * .. bullet
 * ... bullet
 * ... bullet
 * .... bullet
 * .... bullet
 * ... bullet
 * ... bullet
 * .. bullet
 * .. bullet
 * . bullet
 * --
 *
 * Tables::
 * +
 * .An example table
 * [cols="3", options="header"]
 * |===
 * |Column 1
 * |Column 2
 * |Column 3
 * 
 * |1
 * |Item 1
 * |a
 * 
 * |2
 * |Item 2
 * |b
 * 
 * |3
 * |Item 3
 * |c
 * |===
 *
 * Sidebar block::
 * +
 * .Optional Title
 * ****
 * *Sidebar* Block
 *
 * Usage: Notes in a sidebar, naturally.
 * ****
 *
 * Admonitions::
 * +
 * IMPORTANT: Check this out!
 *
 * @author https://github.com/johncarl81[John Ericksen]
 * @version 0.1.0
 * @see org.asciidoctor.Asciidoclet
 * @since 0.1.0
 * @serial (or @serialField or @serialData)
 */
public class Asciidoclet extends Doclet {

    protected static final String INCLUDE_BASEDIR_OPTION = "-include-basedir";

    private static StandardAdapter standardAdapter = new StandardAdapter();
    private static DocletIterator iterator = new DocletIterator();

    /**
     * .Example usage
     * [source,java]
     * exampleDeprecated("do not use");
     *
     * @deprecated for example purposes
     * @exception Exception example
     * @throws RuntimeException example
     * @serialData something else
     * @link Asciidoclet
     */
    public static void exampleDeprecated(String field) throws Exception{
        //noop
    }

    /**
     * Sets the language version to Java 5.
     *
     * _Javadoc spec requirement._
     *
     * @return language version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * Sets the option length to the standard Javadoc option length.
     *
     * _Javadoc spec requirement._
     *
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        if (INCLUDE_BASEDIR_OPTION.equals(option)) {
            return 2;
        }
        return standardAdapter.optionLength(option);
    }

    /**
     * The starting point of Javadoc render.
     *
     * _Javadoc spec requirement._
     *
     * @param rootDoc input class documents
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        String baseDir = getBaseDir(rootDoc.options());
        AsciidoctorRenderer renderer = new AsciidoctorRenderer(baseDir, rootDoc);
        try {
            iterator.render(rootDoc, renderer);
            return standardAdapter.start(rootDoc);
        } finally {
            renderer.cleanup();
        }
    }

    /**
     * Processes the input options by delegating to the standard handler.
     *
     * _Javadoc spec requirement._
     *
     * @param options input option array
     * @param errorReporter error handling
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        boolean hasBaseDir = false;
        for (String option[] : options) {
            if (option.length > 0 && INCLUDE_BASEDIR_OPTION.equals(option[0])) {
                hasBaseDir = true;
            }
        }
        if (!hasBaseDir) {
            errorReporter.printWarning(INCLUDE_BASEDIR_OPTION + " must be present for includes or file reference features.");
        }

        return standardAdapter.validOptions(options, errorReporter);
    }

    protected static String getBaseDir(String[][] options) {
        for (String option[] : options) {
            if (INCLUDE_BASEDIR_OPTION.equals(option[0])) {
                return option[1];
            }
        }
        return null;
    }

    /**
     * _For testing purposes._
     *
     * Allows tests to override the standard adapter.
     *
     * @param adapter
     */
    protected static void setStandardAdapter(StandardAdapter adapter){
        standardAdapter = adapter;
    }

    /**
     * _For testing purposes._
     *
     * Allows tests to override the doclet iterator.
     *
     * @param iterator
     */
    protected static void setIterator(DocletIterator iterator) {
        Asciidoclet.iterator = iterator;
    }
}
