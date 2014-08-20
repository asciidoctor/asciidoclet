package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Tag;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author John Ericksen
 */
public class AsciidoctorRendererTest {

    private static final String BASE_DIR = "testBaseDir";
    private AsciidoctorRenderer renderer;
    private Asciidoctor mockAsciidoctor;

    @Before
    public void setup(){
        mockAsciidoctor = mock(Asciidoctor.class);
        renderer = new AsciidoctorRenderer(DocletOptions.NONE, mock(DocErrorReporter.class), Optional.<OutputTemplates>absent(), mockAsciidoctor);
    }

    @Test
    public void testAtLiteralRender(){
        Doc mockDoc = mock(Doc.class);
        String convertedText = "Test";
        String rawText = "@" + convertedText;

        when(mockDoc.getRawCommentText()).thenReturn(rawText);
        when(mockDoc.commentText()).thenReturn("");
        when(mockDoc.tags()).thenReturn(new Tag[]{});

        renderer.renderDoc(mockDoc);
        verify(mockDoc).setRawCommentText("{@literal @}" + convertedText);
        verify(mockAsciidoctor).render(anyString(), any(Options.class));
    }

    @Test
    public void testTagRender(){
        Doc mockDoc = mock(Doc.class);
        Tag mockTag = mock(Tag.class);

        String tagName = "tagName";
        String tagText = "tagText";
        String asciidoctorRenderedString = "rendered";

        when(mockTag.name()).thenReturn(tagName);
        when(mockTag.text()).thenReturn(tagText);

        when(mockDoc.getRawCommentText()).thenReturn("");
        when(mockDoc.commentText()).thenReturn("");
        when(mockDoc.tags()).thenReturn(new Tag[]{mockTag});

        when(mockAsciidoctor.render(eq(""), argThat(new OptionsMatcher(false)))).thenReturn("");
        when(mockAsciidoctor.render(eq(tagText), argThat(new OptionsMatcher(true)))).thenReturn(asciidoctorRenderedString);

        renderer.renderDoc(mockDoc);

        verify(mockAsciidoctor).render(eq(""), argThat(new OptionsMatcher(false)));
        verify(mockAsciidoctor).render(eq(tagText), argThat(new OptionsMatcher(true)));
        verify(mockDoc).setRawCommentText("");
        verify(mockDoc).setRawCommentText("\n" + tagName + " " + asciidoctorRenderedString + "\n");
    }

    @Test
    public void testCleanInput(){
        assertEquals("test1\ntest2", AsciidoctorRenderer.cleanJavadocInput("  test1\n test2\n"));
        assertEquals("@", AsciidoctorRenderer.cleanJavadocInput("{@literal @}"));
        assertEquals("/*\ntest\n*/", AsciidoctorRenderer.cleanJavadocInput("/*\ntest\n*\\/"));
        assertEquals("&#64;", AsciidoctorRenderer.cleanJavadocInput("{at}"));
        assertEquals("/", AsciidoctorRenderer.cleanJavadocInput("{slash}"));
    }

    private static final class OptionsMatcher extends ArgumentMatcher<Options> {

        private final boolean inline;

        private OptionsMatcher(boolean inline){
            this.inline = inline;
        }

        @Override
        public boolean matches(Object input) {
            Options options = (Options) input;
            return !inline || (options.map().get(Options.DOCTYPE).equals(AsciidoctorRenderer.INLINE_DOCTYPE));
        }
    }
}
