package org.asciidoctor.asciidoclet;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.javadoc.DocErrorReporter;
import org.asciidoctor.Asciidoctor;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AttributesLoaderTest {
    static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private final DocErrorReporter mockErrorReporter = mock(DocErrorReporter.class);

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void testNoAttributes() {
        DocletOptions options = DocletOptions.NONE;
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mock(DocErrorReporter.class));

        Map<String,Object> attrs = loader.load();

        assertTrue(attrs.isEmpty());
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void testOnlyCommandLineAttributes() {
        DocletOptions options = new DocletOptions(new String[][] {
                { "-attributes", "foo=bar; foo2=foo-two; not!; override=override@" }
        });
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mockErrorReporter);

        Map<String,Object> attrs = loader.load();

        assertThat(attrs, Matchers.<String,Object>hasEntry("foo", "bar"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("foo2", "foo-two"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("override", "override@"));
        assertThat(attrs, not(hasKey("not")));
        assertThat(attrs, hasKey("not!"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void testOnlyAttributesFile() throws Exception {
        File attrsFile = createTempFile("attrs.adoc", ATTRS);

        DocletOptions options = new DocletOptions(new String[][] {
                { "-attributes-file", attrsFile.getAbsolutePath() }
        });
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mockErrorReporter);

        Map<String,Object> attrs = loader.load();

        assertThat(attrs, Matchers.<String,Object>hasEntry("foo", "BAR"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("foo2", "BAR-TWO"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("override", "OVERRIDE"));
        assertThat(attrs, hasKey("not"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void testCommandLineAndAttributesFile() throws Exception {
        File attrsFile = createTempFile("attrs.adoc", ATTRS);

        DocletOptions options = new DocletOptions(new String[][] {
                { "-attributes", "foo=bar; not!; override=override@" },
                { "-attributes-file", attrsFile.getAbsolutePath() }
        });
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mockErrorReporter);

        Map<String,Object> attrs = new HashMap<String,Object>(loader.load());

        assertThat(attrs, Matchers.<String,Object>hasEntry("foo", "bar"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("foo2", "bar-TWO"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("override", "OVERRIDE"));
        assertThat(attrs, not(hasKey("not")));
        assertThat(attrs, hasKey("not!"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void testAttributesFileIncludeFromBaseDir() throws Exception {
        File attrsFile = createTempFile("attrs.adoc", "include::attrs-include.adoc[]");
        createTempFile("attrs-include.adoc", ATTRS);

        DocletOptions options = new DocletOptions(new String[][] {
                { "-attributes-file", attrsFile.getAbsolutePath() },
                { "-include-basedir", attrsFile.getParentFile().getAbsolutePath() },

        });
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mockErrorReporter);

        Map<String,Object> attrs = loader.load();

        assertThat(attrs, Matchers.<String,Object>hasEntry("foo", "BAR"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("foo2", "BAR-TWO"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("override", "OVERRIDE"));
        assertThat(attrs, hasKey("not"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void testAttributesFileIncludeFromOtherDir() throws Exception {
        File attrsFile = createTempFile("attrs.adoc", "include::{includedir}/attrs-include.adoc[]");
        createTempFile("foo", "attrs-include.adoc", ATTRS);

        DocletOptions options = new DocletOptions(new String[][] {
                { "-attributes-file", attrsFile.getAbsolutePath() },
                { "-include-basedir", attrsFile.getParentFile().getAbsolutePath() },
                { "-attributes", "includedir=foo" },

        });
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, mockErrorReporter);

        Map<String,Object> attrs = loader.load();

        assertThat(attrs, Matchers.<String,Object>hasEntry("foo", "BAR"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("foo2", "BAR-TWO"));
        assertThat(attrs, Matchers.<String,Object>hasEntry("override", "OVERRIDE"));
        assertThat(attrs, hasKey("not"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    private File createTempFile(String name, String content) throws IOException {
        File f = tmpDir.newFile(name);
        Files.asCharSink(f, Charsets.UTF_8).write(content);
        return f;
    }

    private File createTempFile(String dir, String name, String content) throws IOException {
        File d = tmpDir.newFolder(dir);
        File f = new File(d, name);
        Files.asCharSink(f, Charsets.UTF_8).write(content);
        return f;
    }

    static final String ATTRS =
            ":foo: BAR\n" +
            ":foo2: {foo}-TWO\n" +
            ":not: FOO\n" +
            ":override: OVERRIDE\n";
}
