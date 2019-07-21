/*
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
package org.asciidoctor.asciidoclet;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;

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
public class Asciidoclet implements Doclet
{

    private StandardDoclet standardDoclet;
    private DocletOptions docletOptions;
    private Stylesheets stylesheets;
    private Reporter reporter;

    public Asciidoclet() {
        standardDoclet = new StandardDoclet();
    }

    @Override
    public void init( Locale locale, Reporter reporter )
    {
        this.reporter = reporter;
        standardDoclet.init( locale, reporter );
        this.docletOptions = new DocletOptions( reporter );
        this.stylesheets = new Stylesheets( reporter );
    }

    @Override
    public String getName()
    {
        return "Asciidoclet";
    }

    @Override
    public Set<? extends Option> getSupportedOptions()
    {
        Set<Option> options = new HashSet<>( standardDoclet.getSupportedOptions() );
        Arrays.stream( AsciidocletOptions.values() ).map( o -> new OptionProcessor( o, docletOptions ) ).forEach( options::add );
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean run( DocletEnvironment environment )
    {
        docletOptions.validateOptions();
        AsciidoctorRenderer renderer = new AsciidoctorRenderer( docletOptions, reporter );
        boolean result;
        try ( AsciidoctorFilteredEnvironment env = new AsciidoctorFilteredEnvironment( environment, renderer ) )
        {
            result = standardDoclet.run( env );
        }
        catch ( IOException e )
        {
            throw new UncheckedIOException( e );
        }
        return result && postProcess( environment );
    }

    private boolean postProcess( DocletEnvironment environment ) {
        if (docletOptions.stylesheet().isPresent()) {
            return true;
        }
        return stylesheets.copy( environment );
    }
}
