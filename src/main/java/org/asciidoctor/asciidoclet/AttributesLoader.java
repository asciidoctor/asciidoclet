/*
 * Copyright 2013-2024 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import jdk.javadoc.doclet.Reporter;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.jruby.internal.IOUtils;

import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

class AttributesLoader {

    private final Asciidoctor asciidoctor;
    private final DocletOptions docletOptions;
    private final Reporter errorReporter;

    AttributesLoader(Asciidoctor asciidoctor, DocletOptions docletOptions, Reporter errorReporter) {
        this.asciidoctor = asciidoctor;
        this.docletOptions = docletOptions;
        this.errorReporter = errorReporter;
    }

    Map<String, Object> load() {
        List<String> attributeArgs = docletOptions.attributes();
        Set<String> unset = getUnsetAttributes(attributeArgs);

        // Parse command-line attrs first, if any
        Map<String, Object> cmdlineAttrs = parseCmdLineAttributes(attributeArgs);

        // Parse the attributes file, passing in any command-line attrs already set
        Map<String, Object> attrs = parseAttributesFile(docletOptions.attributesFile(), cmdlineAttrs);

        // Remove any attributes that were set in the file but removed by the -attributes option
        attrs.keySet().removeAll(unset);

        // Put unset attribute names back into the map as "key!", so Asciidoctor will unset
        // those attributes in the document.
        for (String key : unset) {
            attrs.put(key + "!", "");
        }

        return attrs;
    }

    private Map<String, Object> parseCmdLineAttributes(List<String> attributeArgs) {
        return new Attributes(attributeArgs.toArray(new String[0])).map();
    }

    private Map<String, Object> parseAttributesFile(Optional<File> attrsFile, Map<String, Object> cmdlineAttrs) {
        if (attrsFile.isPresent()) {
            try (Reader reader = Files.newBufferedReader(attrsFile.get().toPath(), docletOptions.encoding())) {
                return parseAttributes(reader, cmdlineAttrs);
            } catch (Exception e) {
                errorReporter.print(Diagnostic.Kind.WARNING, "Cannot read attributes file: " + e);
            }
        }
        return cmdlineAttrs;
    }

    private Map<String, Object> parseAttributes(Reader in, Map<String, Object> existingAttrs) {
        OptionsBuilder options = Options.builder()
                .safe(SafeMode.SAFE)
                .attributes(existingAttrs)
                .parseHeaderOnly(true);
        if (docletOptions.baseDir().isPresent()) {
            options.baseDir(docletOptions.baseDir().get());
        }

        final String content = read(in);
        final Map<String, Object> parsed = asciidoctor.load(content, options.build()).getAttributes();
        return parsed;
    }

    public static String read(Reader reader) {
        try (Scanner scanner = new Scanner(reader).useDelimiter("\\A")){
            return scanner.next();
        }
    }

    private Set<String> getUnsetAttributes(List<String> args) {
        Set<String> removed = new HashSet<>();
        for (String arg : args) {
            String key = getKey(arg);
            if (key.startsWith("!") || key.endsWith("!")) {
                removed.add(normalizeAttrName(key));
            }
        }
        return Set.copyOf(removed);
    }

    private String getKey(String arg) {
        int idx = arg.indexOf('=');
        if (idx == 0) {
            throw new IllegalArgumentException("Invalid attribute arg: \"" + arg + "\"");
        }
        return idx == -1 ? arg : arg.substring(0, idx);
    }

    // remove non-word chars in name as asciidoctor would
    private String normalizeAttrName(String name) {
        return name.replaceAll("\\W", "").toLowerCase();
    }
}
