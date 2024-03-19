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

import org.junit.Before;
import org.junit.Test;

import javax.tools.Diagnostic;

import static org.asciidoctor.asciidoclet.Stylesheets.JAVA11_STYLESHEET;
import static org.junit.Assert.assertEquals;

public class StylesheetsTest {

    private Stylesheets stylesheets;
    private StubReporter reporter;

    @Before
    public void setup() {
        reporter = new StubReporter();
        stylesheets = new Stylesheets(reporter);
    }

    @Test
    public void java11ShouldSelectStylesheet11() {
        assertEquals(JAVA11_STYLESHEET, stylesheets.selectStylesheet("11"));
        reporter.assertNoMoreInteractions();
    }

    @Test
    public void unknownJavaShouldSelectLatestStylesheetAndWarn() {
        assertEquals(JAVA11_STYLESHEET, stylesheets.selectStylesheet("42.3.0_12"));
        assertEquals(reporter.pullCall().get(0), Diagnostic.Kind.WARNING);
    }
}
