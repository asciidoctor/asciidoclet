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

import com.sun.javadoc.*;
import com.sun.tools.corba.se.idl.constExpr.Times;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author John Ericksen
 */
public class DocletIteratorTest {

    private DocletRenderer mockRenderer;
    private RootDoc mockDoc;
    private ClassDoc mockClassDoc;
    private PackageDoc mockPackageDoc;
    private FieldDoc mockFieldDoc;
    private FieldDoc mockEnumFieldDoc;
    private ConstructorDoc mockConstructorDoc;
    private MethodDoc mockMethodDoc;

    @Before
    public void setup() {
        mockRenderer = mock(DocletRenderer.class);

        mockDoc = mock(RootDoc.class);
        mockPackageDoc = mock(PackageDoc.class);
        when(mockPackageDoc.tags()).thenReturn(new Tag[]{});
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
        when(classDoc.tags()).thenReturn(new Tag[]{});
        return classDoc;
    }

    @Test
    public void testIteration() {
        new DocletIterator(DocletOptions.NONE).render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockFieldDoc);
        verify(mockRenderer).renderDoc(mockConstructorDoc);
        verify(mockRenderer).renderDoc(mockMethodDoc);
        verify(mockRenderer).renderDoc(mockPackageDoc);
        verify(mockRenderer).renderDoc(mockEnumFieldDoc);
    }

    @Test
    public void testAnnotationIteration() {
        AnnotationTypeDoc mockClassDoc = mockClassDoc(AnnotationTypeDoc.class, mockPackageDoc, mockFieldDoc, mockEnumFieldDoc, mockConstructorDoc, mockMethodDoc);
        AnnotationTypeElementDoc mockAnnotationElement = mock(AnnotationTypeElementDoc.class);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockClassDoc.elements()).thenReturn(new AnnotationTypeElementDoc[]{mockAnnotationElement});
        when(mockClassDoc.name()).thenReturn("@foo");

        new DocletIterator(DocletOptions.NONE).render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockAnnotationElement);
    }

    @Test
    public void testIgnoreNonAsciidocOverview() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "foo.html"}}));

        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockDoc, never()).setRawCommentText(any(String.class));
    }

    @Test
    public void testFailIfAsciidocOverviewNotFound() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "notfound.adoc"}}));

        assertFalse(iterator.render(mockDoc, mockRenderer));
    }

    @Test
    public void testOverviewFound() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "src/main/java/overview.adoc"}}));
        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockRenderer).renderDoc(mockDoc);
    }

    @Test
    public void testInclude() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {
                {DocletOptions.OVERVIEW, "foo.html"},
                {DocletOptions.INCLUDE_FILTER, "org.asciidoctor.test.*"}}));

        when(mockClassDoc.qualifiedName()).thenReturn("org.asciidoctor.test.MockClass");
        when(mockPackageDoc.name()).thenReturn("org.asciidoctor.test");
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);

        reset(mockRenderer);

        when(mockClassDoc.qualifiedName()).thenReturn("org.asciidoctor.outsidefilter.MockClass");
        when(mockPackageDoc.name()).thenReturn("org.asciidoctor.outsidefilter");
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);

        verify(mockRenderer, never()).renderDoc(mockClassDoc);
    }

    @Test
    public void testExclude() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {
                {DocletOptions.OVERVIEW, "foo.html"},
                {DocletOptions.EXCLUDE_FILTER, "org.asciidoctor.test.*"}}));

        when(mockClassDoc.qualifiedName()).thenReturn("org.asciidoctor.test.MockClass");
        when(mockPackageDoc.name()).thenReturn("org.asciidoctor.test");
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer, never()).renderDoc(mockClassDoc);

        reset(mockRenderer);

        when(mockClassDoc.qualifiedName()).thenReturn("org.asciidoctor.outsidefilter.MockClass");
        when(mockPackageDoc.name()).thenReturn("org.asciidoctor.outsidefilter");
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
    }

    @Test
    public void testAsciidocletTag() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {
                {DocletOptions.OVERVIEW, "foo.html"},
                {DocletOptions.EXCLUDE_FILTER, "*"}}));

        when(mockClassDoc.qualifiedName()).thenReturn("org.asciidoctor.test.MockClass");
        when(mockPackageDoc.name()).thenReturn("org.asciidoctor.test");
        when(mockClassDoc.containingPackage()).thenReturn(mockPackageDoc);
        Tag asciidocletTag = mock(Tag.class);
        when(asciidocletTag.name()).thenReturn("@asciidoclet");
        when(mockClassDoc.tags()).thenReturn(new Tag[]{asciidocletTag});

        iterator.render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
    }
}
