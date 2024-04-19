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

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

import javax.lang.model.SourceVersion;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * = Asciidoclet
 * <p>
 * https://github.com/asciidoctor/asciidoclet[Asciidoclet] is a Javadoc Doclet
 * that uses https://asciidoctor.org[Asciidoctor] (via the
 * https://github.com/asciidoctor/asciidoctorj[Asciidoctor Java integration])
 * to interpret https://asciidoc.org[AsciiDoc] markup within Javadoc comments.
 * <p>
 * include::README.adoc[tags=usage]
 * <p>
 * == Examples
 * <p>
 * Custom attributes::
 * `+{project_name}+`;; {project_name}
 * `+{project_desc}+`;; {project_desc}
 * `+{project_version}+`;; {project_version}
 * <p>
 * Code block (with syntax highlighting added by CodeRay)::
 * +
 * [source,java]
 * --
 * /**
 * * = Asciidoclet
 * *
 * * A Javadoc Doclet that uses https://asciidoctor.org[Asciidoctor]
 * * to render https://asciidoc.org[AsciiDoc] markup in Javadoc comments.
 * *
 * * @author https://github.com/johncarl81[John Ericksen]
 * *\/
 * public class Asciidoclet extends Doclet {
 * private final Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
 *
 * @author https://github.com/johncarl81[John Ericksen]
 * @version {project_version}
 * @SuppressWarnings("UnusedDeclaration") public static boolean start(RootDoc rootDoc) {
 * new Asciidoclet().render(rootDoc); // <2>
 * return Standard.start(rootDoc);
 * }
 * }
 * --
 * <1> Creates an instance of the Asciidoctor Java integration
 * <2> Runs Javadoc comment strings through Asciidoctor
 * <p>
 * Inline code:: `code()`
 * <p>
 * Headings::
 * +
 * --
 * [float]
 * = Heading 1
 * <p>
 * [float]
 * == Heading 2
 * <p>
 * [float]
 * === Heading 3
 * <p>
 * [float]
 * ==== Heading 4
 * <p>
 * [float]
 * ===== Heading 5
 * --
 * <p>
 * Links::
 * Doc Writer <doc@example.com> +
 * https://asciidoc.org[AsciiDoc] is a lightweight markup language. +
 * Learn more about it at https://asciidoctor.org. +
 * <p>
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
 * <p>
 * Tables::
 * +
 * .An example table
 * |===
 * |Column 1 |Column 2 |Column 3
 * <p>
 * |1
 * |Item 1
 * |a
 * <p>
 * |2
 * |Item 2
 * |b
 * <p>
 * |3
 * |Item 3
 * |c
 * |===
 * <p>
 * Sidebar block::
 * +
 * .Optional Title
 * ****
 * Usage: Notes in a sidebar, naturally.
 * ****
 * <p>
 * Admonitions::
 * +
 * IMPORTANT: Check this out!
 * @serial (or @ serialField or @ serialData)
 * @see Asciidoclet
 * @since 0.1.0
 */
public class Asciidoclet implements Doclet {

    private StandardDoclet standardDoclet;
    private DocletOptions docletOptions;
    private Stylesheets stylesheets;
    private Reporter reporter;
    
    /**
     * Creates a new {@link Asciidoclet} object.
     */
    public Asciidoclet() {
        standardDoclet = new StandardDoclet();
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
        this.standardDoclet.init(locale, reporter);
        this.docletOptions = new DocletOptions(reporter);
        this.stylesheets = new Stylesheets(reporter);
    }

    @Override
    public String getName() {
        return "Asciidoclet";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        Set<Option> options = new HashSet<>(standardDoclet.getSupportedOptions());
        Arrays.stream(AsciidocletOptions.values()).map(o -> new OptionProcessor(o, docletOptions)).forEach(options::add);
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        docletOptions.validate();
        AsciidoctorConverter converter = new AsciidoctorConverter(docletOptions, reporter);
        boolean result;
        try (AsciidoctorFilteredEnvironment env = new AsciidoctorFilteredEnvironment(environment, converter)) {
            result = standardDoclet.run(env);
        }
        return result && postProcess(environment);
    }

    private boolean postProcess(DocletEnvironment environment) {
        if (docletOptions.stylesheet().isPresent()) {
            return true;
        }
        return stylesheets.copy(environment);
    }
}
