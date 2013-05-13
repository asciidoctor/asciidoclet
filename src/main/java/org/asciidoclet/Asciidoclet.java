package org.asciidoclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.standard.Standard;
import org.asciidoctor.Asciidoctor;

import java.util.*;

/**
 *
 * @author John Ericksen
 */
public class Asciidoclet extends Doclet  {

    private final Map<String, TagRenderer> tagRenderers = new HashMap<String, TagRenderer>();
    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        return Standard.optionLength(option);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        new Asciidoclet().render(rootDoc);

        return Standard.start(rootDoc);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return Standard.validOptions(options, errorReporter);
    }

    private void render(RootDoc rootDoc) {
        Set<PackageDoc> packages = new HashSet<PackageDoc>();
        for ( ClassDoc doc : rootDoc.classes() ) {
            packages.add(doc.containingPackage());
            renderClass(doc);
        }
        for ( PackageDoc doc : packages ) {
            renderDoc(doc);
        }
    }

    private void renderClass(ClassDoc doc) {
        renderDoc(doc);
        for ( MemberDoc member : doc.fields() ) {
            renderDoc(member);
        }
        for ( MemberDoc member : doc.constructors() ) {
            renderDoc(member);
        }
        for ( MemberDoc member : doc.methods() ) {
            renderDoc(member);
        }
        if ( doc instanceof AnnotationTypeDoc ) {
            for ( MemberDoc member : ((AnnotationTypeDoc)doc).elements() ) {
                renderDoc(member);
            }
        }
    }

    private void renderDoc(Doc doc) {
        StringBuilder buffer = new StringBuilder();
        // Replace "\n " to remove default javadoc space.
        String input = doc.commentText().replaceAll("\n ", "\n");
        buffer.append(asciidoctor.render(input, Collections.<String, Object>emptyMap()));
        buffer.append('\n');
        for ( Tag tag : doc.tags() ) {
            renderTag(tag, buffer);
            buffer.append('\n');
        }
        doc.setRawCommentText(buffer.toString());
    }

    private void renderTag(Tag tag, StringBuilder target) {
        TagRenderer renderer = (TagRenderer)tagRenderers.get(tag.kind());
        if (tagRenderers.containsKey(tag.kind())) {
            renderer.render(tag, target);
        }
        else {
            //print out directly
            target.append(tag.name()).append(" ").append(tag.text());
        }
    }
}
