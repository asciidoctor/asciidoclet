/*
 * Copyright 2013-2024 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import org.asciidoctor.asciidoclet.JavadocParser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JavadocParserTest {

    @Test
    public void parsePlainBody() {
        JavadocParser parser = JavadocParser.parse("plain body");
        assertEquals(parser.getCommentBody(), "plain body");
    }

    @Test
    public void parsePlainBodyAndTag() {
        JavadocParser parser = JavadocParser.parse("plain body\n@see OtherPlace");
        assertEquals("plain body", parser.getCommentBody());
        assertEquals(List.of(new Tag("@see", "OtherPlace")), parser.tags());
    }

    @Test
    public void parseTag() {
        JavadocParser parser = JavadocParser.parse("@see Other");
        assertEquals("", parser.getCommentBody());
        assertEquals(List.of(new Tag("@see", "Other")), parser.tags());
    }

    @Test
    public void parseTagWithNewLine() {
        JavadocParser parser = JavadocParser.parse("@see Other\n place");
        assertEquals("", parser.getCommentBody());
        assertEquals(List.of(new Tag("@see", "Other\n place")), parser.tags());
    }

    @Test
    public void parseMultipleTags() {
        JavadocParser parser = JavadocParser.parse("@see Other\n@throws Exception");
        assertEquals("", parser.getCommentBody());
        assertEquals(List.of(new Tag("@see", "Other"), new Tag("@throws", "Exception")), parser.tags());
    }

    @Test
    public void parseMultipleMultiLineTags() {
        JavadocParser parser = JavadocParser.parse("@see Other\n place\nnearby\n@throws Exception\non error");
        assertEquals("", parser.getCommentBody());
        assertEquals(List.of(
                new Tag("@see", "Other\n place\nnearby"),
                new Tag("@throws", "Exception\non error")), parser.tags());
    }

    @Test
    public void parseWithBlockInBody() {
        JavadocParser parser = JavadocParser.parse("Body\n--\n@see bla\n--\n@see foo");
        assertEquals("Body\n--\n@see bla\n--", parser.getCommentBody());
        assertEquals(List.of(new Tag("@see", "foo")), parser.tags());
    }
}
