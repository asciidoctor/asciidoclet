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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

class AsciidocFileView implements JavaFileObject
{
    private final AsciidoctorRenderer renderer;
    private final FileObject fileObject;
    private String renderedContents;

    AsciidocFileView( AsciidoctorRenderer renderer, FileObject fileObject )
    {
        this.renderer = renderer;
        this.fileObject = fileObject;
    }

    @Override
    public URI toUri()
    {
        try
        {
            URI uri = fileObject.toUri();
            uri = new URI( maskFileExtension( uri.toString() ) );
            return uri;
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public String getName()
    {
        return maskFileExtension( fileObject.getName() );
    }

    private String maskFileExtension( String name )
    {
        if ( isAsciidoctorFile( name ) )
        {
            name = name.substring( 0, name.lastIndexOf( '.' ) ) + ".html";
        }
        return name;
    }

    private boolean isAsciidoctorFile( String name )
    {
        return name.endsWith( ".adoc" ) || name.endsWith( ".ad" ) || name.endsWith( ".asciidoc" ) || name.endsWith( ".txt" );
    }

    @Override
    public InputStream openInputStream() throws IOException
    {
        return new ByteArrayInputStream( getCharContent( true ).getBytes( Charset.defaultCharset() ) );
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        return fileObject.openOutputStream();
    }

    @Override
    public Reader openReader( boolean ignoreEncodingErrors ) throws IOException
    {
        return new StringReader( getCharContent( ignoreEncodingErrors ) );
    }

    @Override
    public String getCharContent( boolean ignoreEncodingErrors ) throws IOException
    {
        if ( renderedContents == null )
        {
            renderedContents = fileObject.getCharContent( ignoreEncodingErrors ).toString();
            if ( isAsciidoctorFile( fileObject.getName() ) )
            {
                renderedContents = "<body>" + renderer.renderDoc( renderedContents ) + "</body>";
            }
        }
        return renderedContents;
    }

    @Override
    public Writer openWriter() throws IOException
    {
        return fileObject.openWriter();
    }

    @Override
    public long getLastModified()
    {
        return fileObject.getLastModified();
    }

    @Override
    public boolean delete()
    {
        return fileObject.delete();
    }

    @Override
    public Kind getKind()
    {
        return ((JavaFileObject) fileObject).getKind();
    }

    @Override
    public boolean isNameCompatible( String simpleName, Kind kind )
    {
        return ((JavaFileObject) fileObject).isNameCompatible( simpleName, kind );
    }

    @Override
    public NestingKind getNestingKind()
    {
        return ((JavaFileObject) fileObject).getNestingKind();
    }

    @Override
    public Modifier getAccessLevel()
    {
        return ((JavaFileObject) fileObject).getAccessLevel();
    }

    @SuppressWarnings( "unchecked" )
    <T extends FileObject> T unwrap()
    {
        return (T) fileObject;
    }
}
