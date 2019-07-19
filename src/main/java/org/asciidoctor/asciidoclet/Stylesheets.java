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

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.tools.Diagnostic;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileManager;

/**
 * Responsible for copying the appropriate stylesheet to the javadoc
 * output directory.
 */
public class Stylesheets
{
    static final String JAVA11_STYLESHEET = "stylesheet11.css";
    static final String JAVA9_STYLESHEET = "stylesheet9.css";
    static final String JAVA8_STYLESHEET = "stylesheet8.css";
    static final String JAVA6_STYLESHEET = "stylesheet6.css";
    private static final String CODERAY_STYLESHEET = "coderay-asciidoctor.css";
    private static final String OUTPUT_STYLESHEET = "stylesheet.css";

    private final Reporter errorReporter;

    Stylesheets( Reporter errorReporter )
    {
        this.errorReporter = errorReporter;
    }

    public boolean copy( DocletEnvironment environment )
    {
        String stylesheet = selectStylesheet( System.getProperty( "java.version" ) );
        JavaFileManager fm = environment.getJavaFileManager();
        try ( InputStream stylesheetIn = getResource( stylesheet );
              InputStream coderayStylesheetIn = getResource( CODERAY_STYLESHEET );
              OutputStream stylesheetOut = openOutputStream( fm, OUTPUT_STYLESHEET );
              OutputStream coderayStylesheetOut = openOutputStream( fm, CODERAY_STYLESHEET ) )
        {
            stylesheetIn.transferTo( stylesheetOut );
            coderayStylesheetIn.transferTo( coderayStylesheetOut );
            return true;
        }
        catch ( IOException e )
        {
            errorReporter.print( Diagnostic.Kind.ERROR, e.getLocalizedMessage() );
            return false;
        }
    }

    private OutputStream openOutputStream( JavaFileManager fm, String filename ) throws IOException
    {
        return fm.getFileForOutput( DocumentationTool.Location.DOCUMENTATION_OUTPUT, "", filename, null ).openOutputStream();
    }

    private InputStream getResource( String name ) throws IOException
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if ( loader == null )
        {
            loader = Stylesheets.class.getClassLoader();
        }
        InputStream stream = loader.getResourceAsStream( name );
        if ( stream != null )
        {
            return stream;
        }

        Module module = Stylesheets.class.getModule();
        if ( module != null )
        {
            stream = module.getResourceAsStream( name );
            if ( stream != null )
            {
                return stream;
            }
        }

        throw new IllegalArgumentException( "No such resource: " + name );
    }

    String selectStylesheet( String javaVersion )
    {
        if ( javaVersion.matches( "^1\\.[56]\\D.*" ) )
        {
            return JAVA6_STYLESHEET;
        }
        if ( javaVersion.matches( "^1\\.[78]\\D.*" ) )
        {
            return JAVA8_STYLESHEET;
        }
        if ( javaVersion.matches( "^(9|10)(\\.)?.*" ) )
        {
            return JAVA9_STYLESHEET;
        }
        if ( javaVersion.matches( "^(11)(\\.)?.*" ) )
        {
            return JAVA11_STYLESHEET;
        }
        errorReporter.print( Diagnostic.Kind.WARNING, "Unrecognized Java version " + javaVersion + ", using Java 11 stylesheet" );
        return JAVA11_STYLESHEET;
    }
}
