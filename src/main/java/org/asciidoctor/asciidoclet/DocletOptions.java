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

import jdk.javadoc.doclet.Reporter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Provides an interface to the doclet options we are interested in.
 */
public class DocletOptions
{

    private final Reporter reporter;

    private File basedir;
    private File overview;
    private File stylesheet;
    private File attributesFile;
    private List<String> includeFilters;
    private List<String> excludeFilters;
    private String gemPath;
    private List<String> requires;
    private Charset encoding;
    private List<String> attributes;

    public DocletOptions( Reporter reporter )
    {
        this.reporter = reporter;
        requires = new ArrayList<>();
        encoding = Charset.defaultCharset();
        attributes = new ArrayList<>();
    }

    void collect( AsciidocletOptions option, List<String> list )
    {
        switch ( option )
        {
            case BASEDIR:
                basedir = new File( list.get( 0 ) );
                break;
            case OVERVIEW:
                overview = new File( list.get( 0 ) );
                break;
            case STYLESHEET:
                stylesheet = new File( list.get( 0 ) );
                break;
            case ENCODING:
                encoding = Charset.forName( list.get( 0 ) );
                break;
            case ATTRIBUTE:
                splitTrimStream( list ).forEach( attributes::add );
                break;
            case ATTRIBUTES_FILE:
                attributesFile = new File( list.get( 0 ) );
                break;
            case GEM_PATH:
                gemPath = list.get( 0 );
            case REQUIRE:
            case REQUIRE_LONG:
                splitTrimStream( list ).forEach( requires::add );
                break;
// TODO
//        } else if (INCLUDE_FILTER.equals(option[0])) {
//        includeFilters.add(option[1]);
//    } else if (EXCLUDE_FILTER.equals(option[0])) {
//        excludeFilters.add(option[1]);
//    }
        }
    }

    private Stream<String> splitTrimStream( List<String> list )
    {
        return list.stream()
                .flatMap( s -> Arrays.stream( s.split( "\\s*,\\s*" ) ) )
                .map( String::trim )
                .filter( s -> !s.isEmpty() );
    }

    public Optional<File> stylesheet() {
    Optional<File> overview()
    {
        return Optional.ofNullable( overview );
    }

    Optional<File> stylesheet()
    {
        return Optional.ofNullable( stylesheet );
    }

    Optional<File> baseDir()
    {
        return Optional.ofNullable( basedir );
    }

    Charset encoding()
    {
        return encoding;
    }

    List<String> attributes()
    {
        return attributes;
    }

         List<String> getIncludeFilters() {
            return includeFilters;
        }

         List<String> getExcludeFilters() {
            return excludeFilters;
        }

        Optional<File> attributesFile()
        {
            if ( attributesFile == null )
            {
                return Optional.empty();
            }
            if ( !attributesFile.isAbsolute() && baseDir().isPresent() )
            {
                return Optional.of( new File( baseDir().get(), attributesFile.getPath() ) );
            }
            return Optional.of( attributesFile );
        }

    String gemPath()
    {
        return gemPath;
    }

    List<String> requires()
    {
        return requires;
    }

    // TODO Needed somewhere esle?
        // How are these validated now?
//    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter, StandardAdapter standardDoclet) {
//        DocletOptions docletOptions = new DocletOptions(options);
//
//        if (!docletOptions.baseDir().isPresent()) {
//            errorReporter.printWarning(BASEDIR + " must be present for includes or file reference features to work properly.");
//        }
//
//        Optional<File> attrsFile = docletOptions.attributesFile();
//        if (attrsFile.isPresent() && !attrsFile.get().canRead()) {
//            errorReporter.printWarning("Cannot read attributes file " + attrsFile.get());
//        }
//
//        return standardDoclet.validOptions(options, errorReporter);
//    }
//
//    public static int optionLength(String option, StandardAdapter standardDoclet) {
//        if (BASEDIR.equals(option)) {
//            return 2;
//        }
//        if (ATTRIBUTE.equals(option) || ATTRIBUTE_LONG.equals(option)) {
//            return 2;
//        }
//        if (ATTRIBUTES_FILE.equals(option)) {
//            return 2;
//        }
//        if (GEM_PATH.equals(option)) {
//            return 2;
//        }
//        if (REQUIRE.equals(option) || REQUIRE_LONG.equals(option)) {
//            return 2;
//        }
//        return standardDoclet.optionLength(option);
//    }
}
