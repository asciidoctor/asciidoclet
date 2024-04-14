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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavadocParserTest {

    @Test
    void parsePlainBody() {
        JavadocParser parser = JavadocParser.parse("plain body");
        assertThat(parser.getCommentBody()).isEqualTo("plain body");
    }

    @Test
    void parsePlainBodyAndTag() {
        JavadocParser parser = JavadocParser.parse("plain body\n@see OtherPlace");
        assertThat(parser.getCommentBody()).isEqualTo("plain body");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new Tag("@see", "OtherPlace"));
    }

    @Test
    void parseTag() {
        JavadocParser parser = JavadocParser.parse("@see Other");
        assertThat(parser.getCommentBody()).isEqualTo("");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new Tag("@see", "Other"));
    }

    @Test
    void parseTagWithNewLine() {
        JavadocParser parser = JavadocParser.parse("@see Other\n place");
        assertThat(parser.getCommentBody()).isEqualTo("");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new Tag("@see", "Other\n place"));
    }

    @Test
    void parseMultipleTags() {
        JavadocParser parser = JavadocParser.parse("@see Other\n@throws Exception");
        assertThat(parser.getCommentBody()).isEqualTo("");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new Tag("@see", "Other"), new Tag("@throws", "Exception"));
    }

    @Test
    void parseMultipleMultiLineTags() {
        JavadocParser parser = JavadocParser.parse("@see Other\n place\nnearby\n@throws Exception\non error");
        assertThat(parser.getCommentBody()).isEqualTo("");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new Tag("@see", "Other\n place\nnearby"), new Tag("@throws", "Exception\non error"));
    }

    @Test
    void parseWithBlockInBody() {
        JavadocParser parser = JavadocParser.parse("Body\n--\n@see bla\n--\n@see foo");
        assertThat(parser.getCommentBody()).isEqualTo("Body\n--\n@see bla\n--");
        assertThat(parser.tags())
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new Tag("@see", "foo"));
    }
}
