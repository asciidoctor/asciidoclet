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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;

import static org.asciidoctor.asciidoclet.Stylesheets.JAVA11_STYLESHEET;
import static org.asciidoctor.asciidoclet.Stylesheets.JAVA_STYLESHEET_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class StylesheetsTest {

    private Stylesheets stylesheets;
    private StubReporter reporter;

    @BeforeEach
    void setup() {
        reporter = new StubReporter();
        stylesheets = new Stylesheets(reporter);
    }

    @Test
    void java11ShouldSelectStylesheet11() {
        assertThat(stylesheets.selectStylesheet("11")).isEqualTo(JAVA11_STYLESHEET);
        reporter.assertNoMoreInteractions();
    }

    @Test
    void unknownNewJavaShouldSelectLatestStylesheet() {
        assertThat(stylesheets.selectStylesheet("42.3.0_12")).isEqualTo(String.format(JAVA_STYLESHEET_FORMAT, 17));
        reporter.assertNoMoreInteractions();
    }

    @Test
    void unknownOldJavaShouldSelectJava11StylesheetAndWarn() {
        assertThat(stylesheets.selectStylesheet("9.9.9")).isEqualTo(String.format(JAVA_STYLESHEET_FORMAT, 11));
        assertThat(reporter.pullCall()).first().isEqualTo(Diagnostic.Kind.WARNING);
        reporter.assertNoMoreInteractions();
    }

    @Test
    void java17ShouldSelectStylesheet17() {
        assertThat(stylesheets.selectStylesheet("17")).isEqualTo(String.format(JAVA_STYLESHEET_FORMAT, 17));
        reporter.assertNoMoreInteractions();
    }
}
