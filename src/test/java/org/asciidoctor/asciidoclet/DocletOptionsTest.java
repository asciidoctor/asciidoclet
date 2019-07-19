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

import jdk.javadoc.doclet.Reporter;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings( "OptionalGetWithoutIsPresent" )
public class DocletOptionsTest
{
    private Reporter reporter;

    @Before
    public void setUp()
    {
        reporter = new StubReporter();
    }

    @Test
    public void testGetBaseDir()
    {
        assertFalse( new DocletOptions( reporter ).baseDir().isPresent() );

        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.BASEDIR, List.of( "test" ) );
        options.validateOptions();
        assertEquals( "test", options.baseDir().get().getName() );
    }

    @Test
    public void testEncoding()
    {
        assertEquals( Charset.defaultCharset(), new DocletOptions( reporter ).encoding() );

        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.ENCODING, List.of( "UTF-8" ) );
        options.validateOptions();
        assertEquals( StandardCharsets.UTF_8, options.encoding() );

        options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.ENCODING, List.of( "US-ASCII" ) );
        options.validateOptions();
        assertEquals( StandardCharsets.US_ASCII, options.encoding() );

        options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.ENCODING, List.of( "ISO-8859-1" ) );
        options.validateOptions();
        assertEquals( StandardCharsets.ISO_8859_1, options.encoding() );
    }

    @Test
    public void testOverview()
    {
        assertFalse( new DocletOptions( reporter ).overview().isPresent() );

        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.OVERVIEW, List.of( "test.adoc" ) );
        options.validateOptions();
        assertEquals( "test.adoc", options.overview().get().getName() );
    }

    @Test
    public void testStylesheetFile()
    {
        assertFalse( new DocletOptions( reporter ).stylesheet().isPresent() );

        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.STYLESHEET, List.of( "foo.css" ) );
        options.validateOptions();
        assertEquals( "foo.css", options.stylesheet().get().getName() );
    }

    @Test
    public void testRequires()
    {
        assertTrue( new DocletOptions( reporter ).requires().isEmpty() );

        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.REQUIRE, List.of( "foo", "bar" ) );
        options.validateOptions();
        assertEquals( options.requires(), List.of( "foo", "bar" ) );

        options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.REQUIRE, List.of( "a", "diagrams/awesome" ) );
        options.collect( AsciidocletOptions.REQUIRE_LONG, List.of( "bar" ) );
        options.collect( AsciidocletOptions.REQUIRE_LONG, List.of( "baz,noddy" ) );
        options.validateOptions();
        assertEquals( options.requires(), List.of( "a", "diagrams/awesome", "bar", "baz", "noddy" ) );
    }
}
