package org.asciidoctor.asciidoclet;

import com.sun.javadoc.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
        if ( doc instanceof AnnotationTypeDoc) {
            for ( MemberDoc member : ((AnnotationTypeDoc)doc).elements() ) {
                renderer.renderDoc(member);
            }
        }
    }

    private boolean processOverview(RootDoc rootDoc, DocletRenderer renderer) {
        File overviewFile = getOverviewFile(rootDoc.options());
        if (overviewFile != null) {
            if (isAsciidocFile(overviewFile.getName())) {
                try {
                    String overviewContent = readFile(overviewFile, getEncoding(rootDoc.options()));
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

    private File getOverviewFile(String[][] options) {
        for (String option[] : options) {
            if ("-overview".equals(option[0])) {
                return new File(option[1]);
            }
        }
        return null;
    }

    private Charset getEncoding(String[][] options) {
        for (String option[] : options) {
            if ("-encoding".equals(option[0])) {
                return Charset.forName(option[1]);
            }
        }
        return Charset.forName("UTF-8");
    }

    private static boolean isAsciidocFile(String name) {
        return ASCIIDOC_FILE_PATTERN.matcher(name).matches();
    }

    private static String readFile(File f, Charset charset) throws IOException {
        char[] buf = new char[1024];
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
        try {
            StringBuilder out = new StringBuilder();
            int n;
            while ((n = reader.read(buf)) > 0) {
                out.append(buf, 0, n);
            }
            return out.toString();
        } finally {
            try {
                reader.close();
            } catch (IOException e) { }
        }
    }

    static final Pattern ASCIIDOC_FILE_PATTERN = Pattern.compile("(.*\\.(ad|adoc|txt|asciidoc))");
}
