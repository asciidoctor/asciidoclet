package org.asciidoctor.asciidoclet;

import com.sun.javadoc.*;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author John Ericksen
 */
public class DocletIteratorTest {

    private DocletIterator iterator;
    private DocletRenderer mockRenderer;
    private RootDoc mockDoc;
    private ClassDoc mockClassDoc;
    private PackageDoc mockPackageDoc;
    private FieldDoc mockFieldDoc;
    private FieldDoc mockEnumFieldDoc;
    private ConstructorDoc mockConstructorDoc;
    private MethodDoc mockMethodDoc;

    @Before
    public void setup(){
        mockRenderer = mock(DocletRenderer.class);
        iterator = new DocletIterator();

        mockDoc = mock(RootDoc.class);
        mockPackageDoc = mock(PackageDoc.class);
        mockFieldDoc = mock(FieldDoc.class);
        mockEnumFieldDoc = mock(FieldDoc.class);
        mockConstructorDoc = mock(ConstructorDoc.class);
        mockMethodDoc = mock(MethodDoc.class);
        mockClassDoc = mockClassDoc(ClassDoc.class, mockPackageDoc, mockFieldDoc, mockEnumFieldDoc, mockConstructorDoc, mockMethodDoc);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockDoc.options()).thenReturn(new String[][]{});
    }

    private <T extends ClassDoc> T mockClassDoc(Class<T> type, PackageDoc packageDoc, FieldDoc fieldDoc, FieldDoc enumConstants, ConstructorDoc constructorDoc, MethodDoc methodDoc) {
        T classDoc = mock(type);
        when(classDoc.containingPackage()).thenReturn(packageDoc);
        when(classDoc.fields()).thenReturn(new FieldDoc[]{fieldDoc});
        when(classDoc.constructors()).thenReturn(new ConstructorDoc[]{constructorDoc});
        when(classDoc.methods()).thenReturn(new MethodDoc[]{methodDoc});
        when(classDoc.enumConstants()).thenReturn(new FieldDoc[]{enumConstants});
        return classDoc;
    }

    @Test
    public void testIteration(){
        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockFieldDoc);
        verify(mockRenderer).renderDoc(mockConstructorDoc);
        verify(mockRenderer).renderDoc(mockMethodDoc);
        verify(mockRenderer).renderDoc(mockPackageDoc);
        verify(mockRenderer).renderDoc(mockEnumFieldDoc);
    }

    @Test
    public void testAnnotationIteration(){
        AnnotationTypeDoc mockClassDoc = mockClassDoc(AnnotationTypeDoc.class, mockPackageDoc, mockFieldDoc, mockEnumFieldDoc, mockConstructorDoc, mockMethodDoc);
        AnnotationTypeElementDoc mockAnnotationElement = mock(AnnotationTypeElementDoc.class);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockClassDoc.elements()).thenReturn(new AnnotationTypeElementDoc[]{mockAnnotationElement});

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockAnnotationElement);
    }

    @Test
    public void testIgnoreNonAsciidocOverview() {
        when(mockDoc.options()).thenReturn(new String[][] {{"-overview", "foo.html"}});

        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockDoc, never()).setRawCommentText(any(String.class));
    }

    @Test
    public void testFailIfAsciidocOverviewNotFound() {
        when(mockDoc.options()).thenReturn(new String[][] {{"-overview", "notfound.adoc"}});

        assertFalse(iterator.render(mockDoc, mockRenderer));
    }

    @Test
    public void testOverviewFound() {
        when(mockDoc.options()).thenReturn(new String[][] {{"-overview", "src/main/java/overview.adoc"}});

        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockRenderer).renderDoc(mockDoc);
    }
}
