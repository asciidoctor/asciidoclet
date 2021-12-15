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

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.sun.javadoc.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Iterates over the various elements of a RootDoc, handing off to the DocletRenderer to perform the rendering work.
 *
 * @author John Ericksen
 */
public class DocletIterator {

    private static final String ASCIIDOCLET_TAG = "@asciidoclet";
    private static final Pattern ASCIIDOC_FILE_PATTERN = Pattern.compile("(.*\\.(ad|adoc|txt|asciidoc))");

    private final DocletOptions docletOptions;
    private final AntPathMatcher pathMatcher;

    public DocletIterator(DocletOptions docletOptions) {
        this.docletOptions = docletOptions;
        pathMatcher = new AntPathMatcher.Builder().withTrimTokens().build();
    }

    /**
     * Renders a RootDoc's contents.
     *
     * @param rootDoc
     * @param renderer
     */
    public boolean render(RootDoc rootDoc, DocletRenderer renderer) {
        if (!processOverview(rootDoc, renderer)) {
            return false;
        }
        Set<PackageDoc> packages = new HashSet<PackageDoc>();
        for (ClassDoc doc : rootDoc.classes()) {
            packages.add(doc.containingPackage());
            if(included(doc.qualifiedName(), doc)) {
                renderClass(doc, renderer);
            }
        }
        for (PackageDoc doc : packages) {
            if(included(doc.name(), doc)) {
                renderer.renderDoc(doc);
            }
        }
        return true;
    }

    private boolean included(String name, Doc doc) {
        List<String> includeFilters = docletOptions.getIncludeFilters();
        List<String> excludeFilters = docletOptions.getExcludeFilters();
        boolean includedMatched = matches(includeFilters, name);
        boolean exclutedMatched = matches(excludeFilters, name);;

        boolean result = (includeFilters.isEmpty() || includedMatched) &&
                (excludeFilters.isEmpty() || !exclutedMatched);

        for (Tag tag : doc.tags()) {
            result |= ASCIIDOCLET_TAG.equals(tag.name());
        }

        return result;
    }

    private boolean matches(List<String> patterns, String name) {
        for(String pattern : patterns) {
            if(pathMatcher.isMatch(pattern, name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renders an individual class.
     *
     * @param doc input
     */
    private void renderClass(ClassDoc doc, DocletRenderer renderer) {
        //handle the various parts of the Class doc
        renderer.renderDoc(doc);
        for (MemberDoc member : doc.fields()) {
            renderer.renderDoc(member);
        }
        for (MemberDoc member : doc.constructors()) {
            renderer.renderDoc(member);
        }
        for (MemberDoc member : doc.methods()) {
            renderer.renderDoc(member);
        }
        for (MemberDoc member : doc.enumConstants()) {
            renderer.renderDoc(member);
        }
        if (doc instanceof AnnotationTypeDoc) {
            if(!ASCIIDOCLET_TAG.equals(doc.name())) {
                for (MemberDoc member : ((AnnotationTypeDoc) doc).elements()) {
                    renderer.renderDoc(member);
                }
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
                rootDoc.printNotice("Skipping non-AsciiDoc overview " + overviewFile + ", will be processed by standard Doclet.");
            }
        }
        return true;
    }

    private static boolean isAsciidocFile(String name) {
        return ASCIIDOC_FILE_PATTERN.matcher(name).matches();
    }
}
