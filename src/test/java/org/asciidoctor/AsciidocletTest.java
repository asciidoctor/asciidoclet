package org.asciidoctor;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.asciidoctor.asciidoclet.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author John Ericksen
 */
public class AsciidocletTest {

    private StandardAdapter mockAdapter;
    private DocletIterator mockIterator;
    private Stylesheets mockStylesheets;

    @Before
    public void setup(){
        mockAdapter = mock(StandardAdapter.class);
        mockIterator = mock(DocletIterator.class);
        mockStylesheets = mock(Stylesheets.class);
        when(mockIterator.render(any(RootDoc.class), any(DocletRenderer.class))).thenReturn(true);
        when(mockStylesheets.copy()).thenReturn(true);
    }

    @Test
    public void testVersion(){
        assertEquals(LanguageVersion.JAVA_1_5, Asciidoclet.languageVersion());
    }

    @Test
    public void testIncludeBaseDirOptionLength(){
        assertEquals(2, Asciidoclet.optionLength(DocletOptions.INCLUDE_BASEDIR, mockAdapter));

        verifyZeroInteractions(mockAdapter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testOtherOptionLength(){
        String testParameter = "parameter";
        int returnSize = 42;

        when(mockAdapter.optionLength(eq(testParameter))).thenReturn(returnSize);

        assertEquals(returnSize, Asciidoclet.optionLength(testParameter, mockAdapter));
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testValidBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{DocletOptions.INCLUDE_BASEDIR, ""}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter, mockAdapter));

        verifyZeroInteractions(mockReporter);
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testInvalidBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{""}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter, mockAdapter));

        verify(mockReporter).printWarning(anyString());
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testEmptyBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter, mockAdapter));

        verify(mockReporter).printWarning(anyString());
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testStart(){
        RootDoc mockDoc = mock(RootDoc.class);
        String[][] options = new String[][]{{DocletOptions.INCLUDE_BASEDIR, "test"}};

        when(mockDoc.options()).thenReturn(options);
        when(mockAdapter.start(mockDoc)).thenReturn(true);

        assertTrue(new Asciidoclet(mockDoc, mockIterator, mockStylesheets).start(mockAdapter));

        verify(mockAdapter).start(mockDoc);
        verify(mockIterator).render(eq(mockDoc), any(DocletRenderer.class));
        verify(mockStylesheets).copy();
    }

    @Test
    public void testStylesheetOverride(){
        RootDoc mockDoc = mock(RootDoc.class);
        String[][] options = new String[][]{{DocletOptions.STYLESHEETFILE, "test"}};

        when(mockDoc.options()).thenReturn(options);
        when(mockAdapter.start(mockDoc)).thenReturn(true);

        assertTrue(new Asciidoclet(mockDoc, mockIterator, mockStylesheets).start(mockAdapter));

        verify(mockAdapter).start(mockDoc);
        verify(mockIterator).render(eq(mockDoc), any(DocletRenderer.class));
        verify(mockStylesheets, never()).copy();
    }
}
