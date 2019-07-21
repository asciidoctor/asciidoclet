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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

class AsciidoctorFileManager implements StandardJavaFileManager
{
    private final AsciidoctorRenderer renderer;
    private final StandardJavaFileManager delegate;

    AsciidoctorFileManager( AsciidoctorRenderer renderer, StandardJavaFileManager delegate )
    {
        this.renderer = renderer;
        this.delegate = delegate;
    }

    @Override
    public boolean isSameFile( FileObject a, FileObject b )
    {
        return delegate.isSameFile( a, b );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles( Iterable<? extends File> files )
    {
        return wrap( delegate.getJavaFileObjectsFromFiles( files ) );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths( Iterable<? extends Path> paths )
    {
        return wrap( delegate.getJavaFileObjectsFromPaths( paths ) );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects( File... files )
    {
        return wrap( delegate.getJavaFileObjects( files ) );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects( Path... paths )
    {
        return wrap( delegate.getJavaFileObjects( paths ) );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings( Iterable<String> names )
    {
        return wrap( delegate.getJavaFileObjectsFromStrings( names ) );
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects( String... names )
    {
        return wrap( delegate.getJavaFileObjects( names ) );
    }

    @Override
    public void setLocation( Location location, Iterable<? extends File> files ) throws IOException
    {
        delegate.setLocation( location, files );
    }

    @Override
    public void setLocationFromPaths( Location location, Collection<? extends Path> paths ) throws IOException
    {
        delegate.setLocationFromPaths( location, paths );
    }

    @Override
    public void setLocationForModule( Location location, String moduleName, Collection<? extends Path> paths ) throws IOException
    {
        delegate.setLocationForModule( location, moduleName, paths );
    }

    @Override
    public Iterable<? extends File> getLocation( Location location )
    {
        return delegate.getLocation( location );
    }

    @Override
    public Iterable<? extends Path> getLocationAsPaths( Location location )
    {
        return delegate.getLocationAsPaths( location );
    }

    @Override
    public Path asPath( FileObject file )
    {
        return delegate.asPath( unwrap( file ) );
    }

    @Override
    public void setPathFactory( PathFactory f )
    {
        delegate.setPathFactory( f );
    }

    @Override
    public ClassLoader getClassLoader( Location location )
    {
        return delegate.getClassLoader( location );
    }

    @Override
    public Iterable<JavaFileObject> list( Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse ) throws IOException
    {
        return wrap( delegate.list( location, packageName, kinds, recurse ) );
    }

    @Override
    public String inferBinaryName( Location location, JavaFileObject file )
    {
        return delegate.inferBinaryName( location, unwrap( file ) );
    }

    @Override
    public boolean handleOption( String current, Iterator<String> remaining )
    {
        return delegate.handleOption( current, remaining );
    }

    @Override
    public boolean hasLocation( Location location )
    {
        return delegate.hasLocation( location );
    }

    @Override
    public JavaFileObject getJavaFileForInput( Location location, String className, JavaFileObject.Kind kind ) throws IOException
    {
        return wrap( delegate.getJavaFileForInput( location, className, kind ) );
    }

    @Override
    public JavaFileObject getJavaFileForOutput( Location location, String className, JavaFileObject.Kind kind, FileObject sibling ) throws IOException
    {
        return wrap( delegate.getJavaFileForOutput( location, className, kind, unwrap( sibling ) ) );
    }

    @Override
    public FileObject getFileForInput( Location location, String packageName, String relativeName ) throws IOException
    {
        return wrap( delegate.getFileForInput( location, packageName, relativeName ) );
    }

    @Override
    public FileObject getFileForOutput( Location location, String packageName, String relativeName, FileObject sibling ) throws IOException
    {
        return wrap( delegate.getFileForOutput( location, packageName, relativeName, unwrap( sibling ) ) );
    }

    @Override
    public void flush() throws IOException
    {
        delegate.flush();
    }

    @Override
    public void close() throws IOException
    {
        delegate.close();
    }

    @Override
    public Location getLocationForModule( Location location, String moduleName ) throws IOException
    {
        return delegate.getLocationForModule( location, moduleName );
    }

    @Override
    public Location getLocationForModule( Location location, JavaFileObject fo ) throws IOException
    {
        return delegate.getLocationForModule( location, unwrap( fo ) );
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader( Location location, Class<S> service ) throws IOException
    {
        return delegate.getServiceLoader( location, service );
    }

    @Override
    public String inferModuleName( Location location ) throws IOException
    {
        return delegate.inferModuleName( location );
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules( Location location ) throws IOException
    {
        return delegate.listLocationsForModules( location );
    }

    @Override
    public boolean contains( Location location, FileObject fo ) throws IOException
    {
        return delegate.contains( location, unwrap( fo ) );
    }

    @Override
    public int isSupportedOption( String option )
    {
        return delegate.isSupportedOption( option );
    }

    @SuppressWarnings( "unchecked" )
    private <T extends FileObject> T wrap( T fo )
    {
        return (T) new AsciidocFileView( renderer, fo );
    }

    private <T extends FileObject> Iterable<T> wrap( Iterable<T> fos )
    {
        return () -> StreamSupport.stream( fos.spliterator(), false ).map( this::wrap ).iterator();
    }

    private <T extends FileObject> T unwrap( T fo )
    {
        if ( fo instanceof AsciidocFileView )
        {
            return ((AsciidocFileView) fo).unwrap();
        }
        return fo;
    }
}
