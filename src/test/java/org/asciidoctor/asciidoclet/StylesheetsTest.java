/**
 * Copyright 2013-2015 John Ericksen
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

import com.sun.javadoc.DocErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.asciidoctor.asciidoclet.Stylesheets.JAVA6_STYLESHEET;
import static org.asciidoctor.asciidoclet.Stylesheets.JAVA8_STYLESHEET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class StylesheetsTest {

    private Stylesheets stylesheets;
    private DocErrorReporter mockErrorReporter;

    @Before
    public void setup() throws Exception {
        mockErrorReporter = mock(DocErrorReporter.class);
        stylesheets = new Stylesheets(DocletOptions.NONE, mockErrorReporter);
    }

    @Test
    public void java8ShouldSelectStylesheet8() throws Exception {
        assertEquals(JAVA8_STYLESHEET, stylesheets.selectStylesheet("1.8.0_11"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java7ShouldSelectStylesheet8() throws Exception {
        assertEquals(JAVA8_STYLESHEET, stylesheets.selectStylesheet("1.7.0_51"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java6ShouldSelectStylesheet6() throws Exception {
        assertEquals(JAVA6_STYLESHEET, stylesheets.selectStylesheet("1.6.0_45"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java5ShouldSelectStylesheet6() throws Exception {
        assertEquals(JAVA6_STYLESHEET, stylesheets.selectStylesheet("1.5.0_22"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void unknownJavaShouldSelectStylesheet8AndWarn() throws Exception {
        assertEquals(JAVA8_STYLESHEET, stylesheets.selectStylesheet("42.3.0_12"));
        verify(mockErrorReporter).printWarning(anyString());
    }
}
