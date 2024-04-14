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

import jdk.javadoc.doclet.Reporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocletOptionsTest {

    private Reporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new StubReporter();
    }

    @Test
    void testGetBaseDir() {
        assertThat(new DocletOptions(reporter).baseDir()).isNotPresent();

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.BASEDIR, List.of("test"));
        options.validateOptions();

        assertThat(options.baseDir().get().getName()).isEqualTo("test");
    }

    @Test
    void testAttributes() {
        final String attribute = "attribute-key=attribute-value";
        assertThat(new DocletOptions(reporter).baseDir()).isNotPresent();

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTE, List.of(attribute));
        options.validateOptions();

        assertThat(options.attributes()).hasSize(1);
        assertThat(options.attributes()).first().isEqualTo(attribute);
    }

    @Test
    void testAttributesLong() {
        final String attribute = "attribute-key=attribute-value";
        assertThat(new DocletOptions(reporter).baseDir()).isNotPresent();

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTE_LONG, List.of(attribute));
        options.validateOptions();

        assertThat(options.attributes()).hasSize(1);
        assertThat(options.attributes()).first().isEqualTo(attribute);
    }

    @Test
    void testEncoding() {
        assertThat(new DocletOptions(reporter).encoding()).isEqualTo(Charset.defaultCharset());

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ENCODING, List.of("UTF-8"));
        options.validateOptions();
        assertThat(options.encoding()).isEqualTo(StandardCharsets.UTF_8);

        options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ENCODING, List.of("US-ASCII"));
        options.validateOptions();
        assertThat(options.encoding()).isEqualTo(StandardCharsets.US_ASCII);

        options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ENCODING, List.of("ISO-8859-1"));
        options.validateOptions();
        assertThat(options.encoding()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    void testStylesheetFile() {
        assertThat(new DocletOptions(reporter).stylesheet()).isNotPresent();

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.STYLESHEET, List.of("foo.css"));
        options.validateOptions();
        assertThat(options.stylesheet().get().getName()).isEqualTo("foo.css");
    }

    @Test
    void testRequires() {
        assertThat(new DocletOptions(reporter).requires()).isEmpty();

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.REQUIRE, List.of("foo", "bar"));
        options.validateOptions();
        assertThat(options.requires()).containsExactlyInAnyOrder("foo", "bar");

        options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.REQUIRE, List.of("a", "diagrams/awesome"));
        options.collect(AsciidocletOptions.REQUIRE_LONG, List.of("bar"));
        options.collect(AsciidocletOptions.REQUIRE_LONG, List.of("baz,noddy"));
        options.validateOptions();
        assertThat(options.requires()).containsExactlyInAnyOrder("a", "diagrams/awesome", "bar", "baz", "noddy");
    }
}
