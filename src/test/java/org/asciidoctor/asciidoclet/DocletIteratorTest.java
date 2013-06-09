package org.asciidoctor.asciidoclet;

import com.sun.javadoc.*;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author John Ericksen
 */
public class DocletIteratorTest {

    private DocletIterator iterator;
    private DocletRenderer mockRenderer;

    @Before
    public void setup(){
        mockRenderer = mock(DocletRenderer.class);
        iterator = new DocletIterator();
    }

    @Test
    public void testIteration(){
        RootDoc mockDoc = mock(RootDoc.class);
        ClassDoc mockClassDoc = mock(ClassDoc.class);
        PackageDoc mockPackageDoc = mock(PackageDoc.class);
        FieldDoc mockFieldDoc = mock(FieldDoc.class);
        ConstructorDoc mockConstructorDoc = mock(ConstructorDoc.class);
        MethodDoc mockMethodDoc = mock(MethodDoc.class);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);
        when(mockClassDoc.fields()).thenReturn(new FieldDoc[]{mockFieldDoc});
        when(mockClassDoc.constructors()).thenReturn(new ConstructorDoc[]{mockConstructorDoc});
        when(mockClassDoc.methods()).thenReturn(new MethodDoc[]{mockMethodDoc});

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockFieldDoc);
        verify(mockRenderer).renderDoc(mockConstructorDoc);
        verify(mockRenderer).renderDoc(mockMethodDoc);
        verify(mockRenderer).renderDoc(mockPackageDoc);
    }

    @Test
    public void testAnnotationIteration(){
        RootDoc mockDoc = mock(RootDoc.class);
        AnnotationTypeDoc mockClassDoc = mock(AnnotationTypeDoc.class);
        AnnotationTypeElementDoc mockAnnotationElement = mock(AnnotationTypeElementDoc.class);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockClassDoc.fields()).thenReturn(new FieldDoc[]{});
        when(mockClassDoc.constructors()).thenReturn(new ConstructorDoc[]{});
        when(mockClassDoc.methods()).thenReturn(new MethodDoc[]{});
        when(mockClassDoc.elements()).thenReturn(new AnnotationTypeElementDoc[]{mockAnnotationElement});

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockAnnotationElement);
    }
}
