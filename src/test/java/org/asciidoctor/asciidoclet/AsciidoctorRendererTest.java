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

import org.junit.Before;
import org.junit.Test;

import static org.asciidoctor.asciidoclet.AsciidoctorRenderer.MARKER;
import static org.junit.Assert.assertEquals;

/**
 * @author John Ericksen
 */
public class AsciidoctorRendererTest {

    private AsciidoctorRenderer renderer;
    private StubReporter reporter = new StubReporter();

    @Before
    public void setup() {
        DocletOptions options = new DocletOptions( reporter );
        renderer = new AsciidoctorRenderer( options, reporter);
    }

    @Test
    public void testAtLiteralRender() {
        assertEquals(MARKER + "<p>{@literal @}Test</p>\n", renderer.renderDoc( "{@literal @}Test" ));
    }

    @Test
    public void testTagRender() {
        String rendered = renderer.renderDoc( "input\n@tagName tagText" );
        assertEquals(MARKER + "<p>input</p>\n@tagName tagText\n", rendered);
    }

    @Test
    public void testCleanInput() {
        assertEquals("test1\ntest2", AsciidoctorRenderer.cleanJavadocInput("  test1\n test2\n"));
        assertEquals("/*\ntest\n*/", AsciidoctorRenderer.cleanJavadocInput("/*\ntest\n*\\/"));
        assertEquals("&#64;", AsciidoctorRenderer.cleanJavadocInput("{at}"));
        assertEquals("/", AsciidoctorRenderer.cleanJavadocInput("{slash}"));
    }

    @Test
    public void testParameterWithoutTypeTag()
    {
        assertEquals( MARKER + "<p>comment</p>\n@param p description\n", renderer.renderDoc( "comment\n@param p description" ) );
        assertEquals( MARKER + "<p>comment</p>\n@param p\n", renderer.renderDoc( "comment\n@param p" ) );
        assertEquals( MARKER + "<p>comment</p>\n@param \n", renderer.renderDoc( "comment\n@param" ) );
    }

    @Test
    public void testParamTagWithTypeParameter() {
        String commentText = "comment";
        String param1Name = "T";
        String param1Text = "<" + param1Name + ">";
        String param2Name = "X";
        String param2Desc = "description";
        String param2Text = "<" + param2Name + "> " + param2Desc;
        String sourceText = commentText + "\n@param " + param1Text + "\n@param " + param2Text;

        assertEquals(MARKER + "<p>comment</p>\n@param <T>\n@param <X> description\n", renderer.renderDoc( sourceText ) );
    }
}
