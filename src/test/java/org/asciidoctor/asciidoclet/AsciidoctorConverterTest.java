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

import static org.asciidoctor.asciidoclet.AsciidoctorConverter.MARKER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author John Ericksen
 */
public class AsciidoctorConverterTest {

    // Asciidoctorj < v2.5.12 used OS linebreaks instead of simply \n
    private static final String LINEBREAK = "\r?\n";

    private AsciidoctorConverter converter;
    private StubReporter reporter = new StubReporter();

    @Before
    public void setup() {
        DocletOptions options = new DocletOptions(reporter);
        converter = new AsciidoctorConverter(options, reporter);
    }

    @Test
    public void testAtLiteralRender() {
        String actual = converter.convert("{@literal @}Test");
        assertThat(actual).matches(MARKER + "<p>\\{@literal @}Test</p>" + LINEBREAK);
    }

    @Test
    public void testTagRender() {
        String actual = converter.convert("input\n@tagName tagText");
        assertThat(actual).matches(MARKER + "<p>input</p>" + LINEBREAK + "@tagName tagText" + LINEBREAK);
    }

    @Test
    public void testCleanInput() {
        assertEquals("test1\ntest2", AsciidoctorConverter.cleanJavadocInput("  test1\n test2\n"));
        assertEquals("/*\ntest\n*/", AsciidoctorConverter.cleanJavadocInput("/*\ntest\n*\\/"));
        assertEquals("&#64;", AsciidoctorConverter.cleanJavadocInput("{at}"));
        assertEquals("/", AsciidoctorConverter.cleanJavadocInput("{slash}"));
    }

    @Test
    public void testParameterWithoutTypeTag() {
        assertThat(converter.convert("comment\n@param p description"))
                .matches(MARKER + "<p>comment</p>" + LINEBREAK + "@param p description" + LINEBREAK);
        assertThat(converter.convert("comment\n@param p"))
                .matches(MARKER + "<p>comment</p>" + LINEBREAK + "@param p" + LINEBREAK);
        assertThat(converter.convert("comment\n@param"))
                .matches(MARKER + "<p>comment</p>" + LINEBREAK + "@param " + LINEBREAK);
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

        assertThat(converter.convert(sourceText))
                .matches(MARKER + "<p>comment</p>" + LINEBREAK + "@param <T>" + LINEBREAK + "@param <X> description" + LINEBREAK);
    }
}
