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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Iterates over the various elements of a RootDoc, handing off to the
 * DocletRenderer to perform the rendering work.
 *
 * @author John Ericksen
 */
public class DocletIterator {

	private static final Pattern ASCIIDOC_FILE_PATTERN = Pattern.compile("(.*\\.(ad|adoc|txt|asciidoc))");

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
		if (!processOverview(rootDoc, renderer)) {
			return false;
		}
		ClassDoc[] specifiedClasses = rootDoc.specifiedClasses();
		PackageDoc[] specifiedPackages = rootDoc.specifiedPackages();
		Set<PackageDoc> packages = new HashSet<>();
		if (specifiedClasses != null && specifiedClasses.length > 0) {
			for (ClassDoc doc : specifiedClasses) {
				packages.add(doc.containingPackage());
				renderClass(doc, renderer);
			}
		} else {
			for (ClassDoc doc : rootDoc.classes()) {
				packages.add(doc.containingPackage());
				renderClass(doc, renderer);
			}
		}
		if (specifiedPackages != null && specifiedPackages.length > 0) {
			for (PackageDoc doc : specifiedPackages) {
				renderer.renderDoc(doc);
			}
		} else {
			for (PackageDoc doc : packages) {
				renderer.renderDoc(doc);
			}
		}

		return true;
	}

	/**
	 * Renders an individual class.
	 *
	 * @param doc input
	 */
	private void renderClass(ClassDoc doc, DocletRenderer renderer) {
		// handle the various parts of the Class doc
		renderer.renderDoc(doc);
		for (MemberDoc member : doc.fields()) {
			System.out.println("  fielddoc " + doc.name());
			renderer.renderDoc(member);
		}
		for (MemberDoc member : doc.constructors()) {
			System.out.println("  constructordoc " + doc.name());
			renderer.renderDoc(member);
		}
		for (MemberDoc member : doc.methods()) {
			System.out.println("  methoddoc " + doc.name());
			renderer.renderDoc(member);
		}
		for (MemberDoc member : doc.enumConstants()) {
			System.out.println("  enumdoc " + doc.name());
			renderer.renderDoc(member);
		}
		if (doc instanceof AnnotationTypeDoc) {
			for (MemberDoc member : ((AnnotationTypeDoc) doc).elements()) {
				System.out.println("  annotationdoc " + doc.name());
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
					String overviewContent = Files.asCharSource(overviewFile, docletOptions.encoding()).read();
					rootDoc.setRawCommentText(overviewContent);
					renderer.renderDoc(rootDoc);
				} catch (IOException e) {
					rootDoc.printError("Error reading overview file: " + e.getLocalizedMessage());
					return false;
				}
			} else {
				rootDoc.printNotice(
						"Skipping non-AsciiDoc overview " + overviewFile + ", will be processed by standard Doclet.");
			}
		}
		return true;
	}

	private static boolean isAsciidocFile(String name) {
		return ASCIIDOC_FILE_PATTERN.matcher(name).matches();
	}
}
