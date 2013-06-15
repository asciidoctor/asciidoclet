package org.asciidoctor;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.asciidoctor.asciidoclet.DocletIterator;
import org.asciidoctor.asciidoclet.DocletRenderer;
import org.asciidoctor.asciidoclet.StandardAdapter;
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

    @Before
    public void setup(){
        mockAdapter = mock(StandardAdapter.class);
        mockIterator = mock(DocletIterator.class);
        Asciidoclet.setStandardAdapter(mockAdapter);
        Asciidoclet.setIterator(mockIterator);
    }

    @Test
    public void testVersion(){
        assertEquals(LanguageVersion.JAVA_1_5, Asciidoclet.languageVersion());
    }

    @Test
    public void testIncludeBaseDirOptionLength(){
        assertEquals(2, Asciidoclet.optionLength(Asciidoclet.INCLUDE_BASEDIR_OPTION));

        verifyZeroInteractions(mockAdapter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testOtherOptionLength(){
        String testParameter = "parameter";
        int returnSize = 42;

        when(mockAdapter.optionLength(eq(testParameter))).thenReturn(returnSize);

        assertEquals(returnSize, Asciidoclet.optionLength(testParameter));
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testValidBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{Asciidoclet.INCLUDE_BASEDIR_OPTION, ""}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter));

        verifyZeroInteractions(mockReporter);
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testInvalidBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{""}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter));

        verify(mockReporter).printWarning(anyString());
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testEmptyBaseDirOption(){
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] inputOptions = new String[][]{{}};

        when(mockAdapter.validOptions(inputOptions, mockReporter)).thenReturn(true);

        assertTrue(Asciidoclet.validOptions(inputOptions, mockReporter));

        verify(mockReporter).printWarning(anyString());
        verify(mockAdapter).validOptions(inputOptions, mockReporter);
        verifyZeroInteractions(mockIterator);
    }

    @Test
    public void testGetBaseDir(){
        String testOptionValue = "test";
        assertEquals(testOptionValue, Asciidoclet.getBaseDir(new String[][]{{Asciidoclet.INCLUDE_BASEDIR_OPTION, testOptionValue}}));
        assertNull(Asciidoclet.getBaseDir(new String[][]{{"notbasedir", testOptionValue}}));
    }

    @Test
    public void testStart(){
        RootDoc mockDoc = mock(RootDoc.class);
        String[][] options = new String[][]{{Asciidoclet.INCLUDE_BASEDIR_OPTION, "test"}};

        when(mockDoc.options()).thenReturn(options);
        when(mockAdapter.start(mockDoc)).thenReturn(true);

        assertTrue(Asciidoclet.start(mockDoc));

        verify(mockAdapter).start(mockDoc);
        verify(mockIterator).render(eq(mockDoc), any(DocletRenderer.class));
    }
}
