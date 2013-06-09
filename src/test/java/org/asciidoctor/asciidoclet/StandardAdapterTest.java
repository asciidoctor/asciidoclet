package org.asciidoctor.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * @author John Ericksen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Standard.class)
public class StandardAdapterTest {

    private StandardAdapter adapter;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(Standard.class);
        adapter = new StandardAdapter();
    }

    @Test
    public void testOptionLength() throws Exception {
        String options = "options";
        int optionsLength = 42;

        PowerMockito.when(Standard.class, "optionLength", options).thenReturn(optionsLength);

        assertEquals(optionsLength, adapter.optionLength(options));

        PowerMockito.verifyStatic();
        Standard.optionLength(options);
    }

    @Test
    public void testStart() throws Exception {
        RootDoc mockDoc = mock(RootDoc.class);

        PowerMockito.when(Standard.class, "start", mockDoc).thenReturn(true);

        assertTrue(adapter.start(mockDoc));

        PowerMockito.verifyStatic();
        Standard.start(mockDoc);
    }

    @Test
    public void testValidOptions() throws Exception {
        DocErrorReporter mockReporter = mock(DocErrorReporter.class);
        String[][] options = new String[][]{{"test"}};

        PowerMockito.when(Standard.class, "validOptions", options, mockReporter).thenReturn(true);

        assertTrue(adapter.validOptions(options, mockReporter));

        PowerMockito.verifyStatic();
        Standard.validOptions(options, mockReporter);
    }
}
