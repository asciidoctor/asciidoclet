package org.asciidoctor.asciidoclet;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.asciidoctor.asciidoclet.DocletOptions.*;

public class DocletOptionsTest {

    @Test
    public void testGetBaseDir(){
        assertFalse(DocletOptions.NONE.baseDir().isPresent());
        assertEquals("test", new DocletOptions(new String[][]{{BASEDIR, "test"}}).baseDir().get().getName());
    }

    @Test
    public void testEncoding() {
        assertEquals(Charset.defaultCharset(), DocletOptions.NONE.encoding());
        assertEquals(Charsets.UTF_8, new DocletOptions(new String[][]{{ENCODING, "UTF-8"}}).encoding());
        assertEquals(Charsets.US_ASCII, new DocletOptions(new String[][]{{ENCODING, "US-ASCII"}}).encoding());
        assertEquals(Charsets.ISO_8859_1, new DocletOptions(new String[][]{{ENCODING, "ISO-8859-1"}}).encoding());
    }

    @Test
    public void testOverview() {
        assertFalse(DocletOptions.NONE.overview().isPresent());
        assertEquals("test.adoc", new DocletOptions(new String[][]{{OVERVIEW, "test.adoc"}}).overview().get().getName());
    }

    @Test
    public void testStylesheetFile() {
        assertFalse(DocletOptions.NONE.stylesheet().isPresent());
        assertEquals("foo.css", new DocletOptions(new String[][]{{STYLESHEET, "foo.css"}}).stylesheet().get().getName());
    }

    @Test
    public void testDestDir() {
        assertFalse(DocletOptions.NONE.destDir().isPresent());
        assertEquals("target", new DocletOptions(new String[][]{{DESTDIR, "target"}}).destDir().get().getName());
    }
}
