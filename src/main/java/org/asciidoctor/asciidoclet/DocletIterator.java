package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.sun.javadoc.*;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Iterates over the various elements of a RootDoc, handing off to the DocletRenderer to perform the rendering work.
 *
 * @author John Ericksen
 */
public class DocletIterator {

    private final DocletOptions docletOptions;

    public DocletIterator(DocletOptions docletOptions) {
        this.docletOptions = docletOptions;
    }

    /**
     * Renders a RootDoc's contents.
     *
     * @param rootDoc
     * @param renderer
     */
    public boolean render(RootDoc rootDoc, DocletRenderer renderer) {
        if (!processOverview(rootDoc, renderer)) return false;
        Set<PackageDoc> packages = new HashSet<PackageDoc>();
        for (ClassDoc doc : rootDoc.classes()) {
            packages.add(doc.containingPackage());
            renderClass(doc, renderer);
        }
        for (PackageDoc doc : packages) {
            renderer.renderDoc(doc);
        }
        return true;
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
        for ( MemberDoc member : doc.enumConstants() ) {
            renderer.renderDoc(member);
        }
        if ( doc instanceof AnnotationTypeDoc) {
            for ( MemberDoc member : ((AnnotationTypeDoc)doc).elements() ) {
                renderer.renderDoc(member);
            }
        }
    }

    private boolean processOverview(RootDoc rootDoc, DocletRenderer renderer) {
        Optional<File> overview = docletOptions.overview();
        if (overview.isPresent()) {
            File overviewFile = overview.get();
            if (isAsciidocFile(overviewFile.getName())) {
                try {
                    String overviewContent = Files.toString(overviewFile, docletOptions.encoding());
                    rootDoc.setRawCommentText(overviewContent);
                    renderer.renderDoc(rootDoc);
                } catch (IOException e) {
                    rootDoc.printError("Error reading overview file: " + e.getLocalizedMessage());
                    return false;
                }
            }
            else {
                rootDoc.printNotice("Skipping non-Asciidoc overview " + overviewFile + ", will be processed by standard doclet.");
            }
        }
        return true;
    }

    private static boolean isAsciidocFile(String name) {
        return ASCIIDOC_FILE_PATTERN.matcher(name).matches();
    }

    static final Pattern ASCIIDOC_FILE_PATTERN = Pattern.compile("(.*\\.(ad|adoc|txt|asciidoc))");
}
