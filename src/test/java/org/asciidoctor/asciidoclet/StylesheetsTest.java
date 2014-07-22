package org.asciidoctor.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.asciidoctor.asciidoclet.Stylesheets.*;

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
        assertEquals(JAVA8_STYLESHEET_RESOURCE, stylesheets.selectStylesheet("1.8.0_11"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java7ShouldSelectStylesheet8() throws Exception {
        assertEquals(JAVA8_STYLESHEET_RESOURCE, stylesheets.selectStylesheet("1.7.0_51"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java6ShouldSelectStylesheet6() throws Exception {
        assertEquals(JAVA6_STYLESHEET_RESOURCE, stylesheets.selectStylesheet("1.6.0_45"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java5ShouldSelectStylesheet6() throws Exception {
        assertEquals(JAVA6_STYLESHEET_RESOURCE, stylesheets.selectStylesheet("1.5.0_22"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void unknownJavaShouldSelectStylesheet8AndWarn() throws Exception {
        assertEquals(JAVA8_STYLESHEET_RESOURCE, stylesheets.selectStylesheet("42.3.0_12"));
        verify(mockErrorReporter).printWarning(anyString());
    }
}
