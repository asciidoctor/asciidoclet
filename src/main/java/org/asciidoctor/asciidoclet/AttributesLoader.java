package org.asciidoctor.asciidoclet;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.sun.javadoc.DocErrorReporter;
import org.asciidoctor.*;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AttributesLoader {
    private final Asciidoctor asciidoctor;
    private final DocletOptions docletOptions;
    private final DocErrorReporter errorReporter;

    AttributesLoader(Asciidoctor asciidoctor, DocletOptions docletOptions, DocErrorReporter errorReporter) {
        this.asciidoctor = asciidoctor;
        this.docletOptions = docletOptions;
        this.errorReporter = errorReporter;
    }

    Map<String,Object> load() {
        List<String> attributeArgs = docletOptions.attributes();
        Set<String> unset = getUnsetAttributes(attributeArgs);

        // Parse command-line attrs first, if any
        Map<String,Object> cmdlineAttrs = parseCmdLineAttributes(attributeArgs);

        // Parse the attributes file, passing in any command-line attrs already set
        Map<String,Object> attrs = parseAttributesFile(docletOptions.attributesFile(), cmdlineAttrs);

        // Remove any attributes that were set in the file but removed by the -attributes option
        attrs.keySet().removeAll(unset);

        // Put unset attribute names back into the map as "key!", so Asciidoctor will unset
        // those attributes in the document.
        for (String key : unset) attrs.put(key + "!", "");

        return attrs;
    }

    private Map<String,Object> parseCmdLineAttributes(List<String> attributeArgs) {
        return new Attributes(attributeArgs.toArray(new String[attributeArgs.size()])).map();
    }

    private Map<String,Object> parseAttributesFile(Optional<File> attrsFile, Map<String,Object> cmdlineAttrs) {
        if (attrsFile.isPresent()) {
            try {
                return parseAttributes(Files.newReader(attrsFile.get(), docletOptions.encoding()), cmdlineAttrs);
            } catch (Exception e) {
                errorReporter.printWarning("Cannot read attributes file: " + e);
            }
        }
        return cmdlineAttrs;
    }

    private Map<String,Object> parseAttributes(Reader in, Map<String,Object> existingAttrs) {
        OptionsBuilder options = OptionsBuilder.options()
                .safe(SafeMode.SAFE)
                .eruby("erubis")
                .attributes(existingAttrs);
        if (docletOptions.includeBasedir().isPresent()) options.baseDir(docletOptions.includeBasedir().get());
        Map<String,Object> parsed = asciidoctor.readDocumentStructure(in, options.get().map()).getHeader().getAttributes();
        // workaround for https://github.com/asciidoctor/asciidoctorj/pull/169
        return new HashMap<String,Object>(parsed);
    }

    private Set<String> getUnsetAttributes(List<String> args) {
        ImmutableSet.Builder<String> removed = ImmutableSet.builder();
        for (String arg : args) {
            String key = getKey(arg);
            if (key.startsWith("!") || key.endsWith("!"))
                removed.add(normalizeAttrName(key));
        }
        return removed.build();
    }

    private String getKey(String arg) {
        int idx = arg.indexOf('=');
        if (idx == 0) throw new IllegalArgumentException("Invalid attribute arg: \"" + arg + "\"");
        return idx == -1 ? arg : arg.substring(0, idx);
    }

    // remove non-word chars in name as asciidoctor would
    private String normalizeAttrName(String name) {
        return name.replaceAll("\\W", "").toLowerCase();
    }
}
