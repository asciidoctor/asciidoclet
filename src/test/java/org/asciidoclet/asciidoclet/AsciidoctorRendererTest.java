/**
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
package org.asciidoclet.asciidoclet;

import com.google.common.base.Optional;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import org.asciidoclet.asciidoclet.AsciidoctorRenderer;
import org.asciidoclet.asciidoclet.DocletOptions;
import org.asciidoclet.asciidoclet.OutputTemplates;
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
    public void setup() {
        mockAsciidoctor = mock(Asciidoctor.class);
        renderer = new AsciidoctorRenderer( DocletOptions.NONE, mock(DocErrorReporter.class), Optional.<OutputTemplates>absent(), mockAsciidoctor);
    }

    @Test
    public void testAtLiteralRender() {
        Doc mockDoc = mock(Doc.class);
        String convertedText = "Test";
        String rawText = "@" + convertedText;

        when(mockDoc.getRawCommentText()).thenReturn(rawText);
        when(mockDoc.commentText()).thenReturn("input");
        when(mockDoc.tags()).thenReturn(new Tag[]{});
        when(mockAsciidoctor.render(anyString(), any(Options.class))).thenReturn("input");

        renderer.renderDoc(mockDoc);
        verify(mockDoc).setRawCommentText("{@literal @}" + convertedText);
        verify(mockAsciidoctor).render(anyString(), any(Options.class));
    }

    @Test
    public void testTagRender() {
        Doc mockDoc = mock(Doc.class);
        Tag mockTag = mock(Tag.class);

        String tagName = "tagName";
        String tagText = "tagText";
        String asciidoctorRenderedString = "rendered";

        when(mockTag.name()).thenReturn(tagName);
        when(mockTag.text()).thenReturn(tagText);

        when(mockDoc.getRawCommentText()).thenReturn("input");
        when(mockDoc.commentText()).thenReturn("input");
        when(mockDoc.tags()).thenReturn(new Tag[]{mockTag});

        when(mockAsciidoctor.render(eq("input"), argThat(new OptionsMatcher(false)))).thenReturn("input");
        when(mockAsciidoctor.render(eq(tagText), argThat(new OptionsMatcher(true)))).thenReturn(asciidoctorRenderedString);

        renderer.renderDoc(mockDoc);

        verify(mockAsciidoctor).render(eq("input"), argThat(new OptionsMatcher(false)));
        verify(mockAsciidoctor).render(eq(tagText), argThat(new OptionsMatcher(true)));
        verify(mockDoc).setRawCommentText("input");
        verify(mockDoc).setRawCommentText("input\n" + tagName + " " + asciidoctorRenderedString + "\n");
    }

    @Test
    public void testCleanInput() {
        assertEquals("test1\ntest2", AsciidoctorRenderer.cleanJavadocInput("  test1\n test2\n"));
        assertEquals("@", AsciidoctorRenderer.cleanJavadocInput("{@literal @}"));
        assertEquals("/*\ntest\n*/", AsciidoctorRenderer.cleanJavadocInput("/*\ntest\n*\\/"));
        assertEquals("&#64;", AsciidoctorRenderer.cleanJavadocInput("{at}"));
        assertEquals("/", AsciidoctorRenderer.cleanJavadocInput("{slash}"));
    }

    @Test
    public void testParamTagWithTypeParameter() {
        String commentText = "comment";
        String param1Name = "T";
        String param1Desc = "";
        String param1Text = "<" + param1Name + ">";
        String param2Name = "X";
        String param2Desc = "description";
        String param2Text = "<" + param2Name + "> " + param2Desc;
        String sourceText = commentText + "\n@param " + param1Text + "\n@param " + param2Text;

        Doc mockDoc = mock(Doc.class);
        when(mockDoc.getRawCommentText()).thenReturn(sourceText);
        when(mockDoc.commentText()).thenReturn(commentText);
        Tag[] tags = new Tag[2];
        ParamTag mockTag1 = mock(ParamTag.class);
        when(mockTag1.name()).thenReturn("@param");
        when(mockTag1.isTypeParameter()).thenReturn(true);
        when(mockTag1.parameterName()).thenReturn(param1Name);
        when(mockTag1.parameterComment()).thenReturn(param1Desc);
        tags[0] = mockTag1;
        ParamTag mockTag2 = mock(ParamTag.class);
        when(mockTag2.name()).thenReturn("@param");
        when(mockTag2.isTypeParameter()).thenReturn(true);
        when(mockTag2.parameterName()).thenReturn(param2Name);
        when(mockTag2.parameterComment()).thenReturn(param2Desc);
        tags[1] = mockTag2;
        when(mockDoc.tags()).thenReturn(tags);
        when(mockAsciidoctor.render(eq(commentText), any(Options.class))).thenReturn(commentText);
        when(mockAsciidoctor.render(eq(param2Desc), any(Options.class))).thenReturn(param2Desc);

        renderer.renderDoc(mockDoc);

        verify(mockAsciidoctor).render(eq(commentText), argThat(new OptionsMatcher(false)));
        verify(mockAsciidoctor).render(eq(param2Desc), argThat(new OptionsMatcher(true)));
        // fixture step
        verify(mockDoc).setRawCommentText(eq(sourceText));
        // result step
        verify(mockDoc).setRawCommentText(eq(sourceText));
    }

    private static final class OptionsMatcher extends ArgumentMatcher<Options> {

        private final boolean inline;

        private OptionsMatcher(boolean inline) {
            this.inline = inline;
        }

        @Override
        public boolean matches(Object input) {
            Options options = (Options) input;
            return !inline || (options.map().get(Options.DOCTYPE).equals(AsciidoctorRenderer.INLINE_DOCTYPE));
        }
    }
}
