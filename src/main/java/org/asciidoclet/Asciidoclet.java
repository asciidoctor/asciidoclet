/**
 * Copyright 2013-2018 John Ericksen
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
package org.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.asciidoclet.asciidoclet.AsciidoctorRenderer;
import org.asciidoclet.asciidoclet.DocletIterator;
import org.asciidoclet.asciidoclet.DocletOptions;
import org.asciidoclet.asciidoclet.StandardAdapter;
import org.asciidoclet.asciidoclet.Stylesheets;

/**
 * = Asciidoclet
 *
 * https://github.com/asciidoctor/asciidoclet[Asciidoclet] is a Javadoc Doclet
 * that uses http://asciidoctor.org[Asciidoctor] (via the
 * https://github.com/asciidoctor/asciidoctorj[Asciidoctor Java integration])
 * to interpet http://asciidoc.org[AsciiDoc] markup within Javadoc comments.
 *
 * include::README.adoc[tags=usage]
 *
 * == Examples
 *
 * Custom attributes::
 * `+{project_name}+`;; {project_name}
 * `+{project_desc}+`;; {project_desc}
 * `+{project_version}+`;; {project_version}
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
 * Inline code:: `code()`
 *
 * Headings::
 * +
 * --
 * [float]
 * = Heading 1
 *
 * [float]
 * == Heading 2
 *
 * [float]
 * === Heading 3
 *
 * [float]
 * ==== Heading 4
 *
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
 * |===
 * |Column 1 |Column 2 |Column 3
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
 * Usage: Notes in a sidebar, naturally.
 * ****
 *
 * Admonitions::
 * +
 * IMPORTANT: Check this out!
 *
 * @author https://github.com/johncarl81[John Ericksen]
 * @version {project_version}
 * @see Asciidoclet
 * @since 0.1.0
 * @serial (or @serialField or @serialData)
 */
public class Asciidoclet extends Doclet {

    private final RootDoc rootDoc;
    private final DocletOptions docletOptions;
    private final DocletIterator iterator;
    private final Stylesheets stylesheets;

    public Asciidoclet(RootDoc rootDoc) {
        this.rootDoc = rootDoc;
        this.docletOptions = new DocletOptions(rootDoc);
        this.iterator = new DocletIterator(docletOptions);
        this.stylesheets = new Stylesheets(docletOptions, rootDoc);
    }

    // test use
    Asciidoclet(RootDoc rootDoc, DocletIterator iterator, Stylesheets stylesheets) {
        this.rootDoc = rootDoc;
        this.docletOptions = new DocletOptions(rootDoc);
        this.iterator = iterator;
        this.stylesheets = stylesheets;
    }

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
    public static void exampleDeprecated(String field) throws Exception {
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
        return optionLength(option, new StandardAdapter());
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
        return new Asciidoclet(rootDoc).start(new StandardAdapter());
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
        return validOptions(options, errorReporter, new StandardAdapter());
    }

    static int optionLength(String option, StandardAdapter standardDoclet) {
        return DocletOptions.optionLength(option, standardDoclet);
    }

    static boolean validOptions(String[][] options, DocErrorReporter errorReporter, StandardAdapter standardDoclet) {
        return DocletOptions.validOptions(options, errorReporter, standardDoclet);
    }

    boolean start(StandardAdapter standardDoclet) {
        return run(standardDoclet)
                && postProcess();
    }

    private boolean run(StandardAdapter standardDoclet) {
        AsciidoctorRenderer renderer = new AsciidoctorRenderer(docletOptions, rootDoc);
        try {
            return iterator.render(rootDoc, renderer) &&
                    standardDoclet.start(rootDoc);
        } finally {
            renderer.cleanup();
        }
    }

    private boolean postProcess() {
        if (docletOptions.stylesheet().isPresent()) {
            return true;
        }
        return stylesheets.copy();
    }
}
