package org.asciidoctor;

import com.sun.javadoc.LanguageVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author John Ericksen
 */
public class AsciidocletTest {

    @Test
    public void testVersion(){
        assertEquals(LanguageVersion.JAVA_1_5, Asciidoclet.languageVersion());
    }

    @Test
    public void testIncludeBaseDirOptionLength(){
        assertEquals(2, Asciidoclet.optionLength(Asciidoclet.INCLUDE_BASEDIR_OPTION));
    }
}
