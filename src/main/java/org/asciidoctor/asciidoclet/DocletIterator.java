package org.asciidoctor.asciidoclet;

import com.sun.javadoc.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Iterates over the various elements of a RootDoc, handing off to the DocletRenderer to perform the rendering work.
 *
 * @author John Ericksen
 */
public class DocletIterator {

    /**
     * Renders a RootDoc's contents.
     *
     * @param rootDoc
     * @param renderer
     */
    public void render(RootDoc rootDoc, DocletRenderer renderer) {
        Set<PackageDoc> packages = new HashSet<PackageDoc>();
        for (ClassDoc doc : rootDoc.classes()) {
            packages.add(doc.containingPackage());
            renderClass(doc, renderer);
        }
        for (PackageDoc doc : packages) {
            renderer.renderDoc(doc);
        }
    }

    /**
     * Renders an individual class.
     *
     * @param doc input
     */
    private void renderClass(ClassDoc doc, DocletRenderer renderer) {
        //handle the various parts of the Class doc
        renderer.renderDoc(doc);
        for ( MemberDoc member : doc.fields() ) {
            renderer.renderDoc(member);
        }
        for ( MemberDoc member : doc.constructors() ) {
            renderer.renderDoc(member);
        }
        for ( MemberDoc member : doc.methods() ) {
            renderer.renderDoc(member);
        }
        if ( doc instanceof AnnotationTypeDoc) {
            for ( MemberDoc member : ((AnnotationTypeDoc)doc).elements() ) {
                renderer.renderDoc(member);
            }
        }
    }
}
