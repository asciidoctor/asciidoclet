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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Provides an interface to the doclet options we are interested in.
 */
public class DocletOptions {

    private final Reporter reporter;

    private Charset encoding;
    private File basedir;
    private File stylesheet;
    private File attributesFile;
    private List<String> attributes;
    private String gemPath;
    private List<String> requires;
    
    /**
     * Creates an {@link DocletOptions} object with a given {@link Reporter} object.
     * @param reporter A {@link Reporter} object with which a new object is created.
     */
    public DocletOptions(Reporter reporter) {
        this.reporter = reporter;
        encoding = Charset.defaultCharset();
        attributes = new ArrayList<>();
        requires = new ArrayList<>();
    }

    void collect(AsciidocletOptions option, List<String> list) {
        switch (option) {
            case ENCODING:
                encoding = Charset.forName(list.get(0));
                break;
            case BASEDIR:
                basedir = new File(list.get(0));
                break;
            case STYLESHEET:
                stylesheet = new File(list.get(0));
                break;
            case ATTRIBUTE:
            case ATTRIBUTE_LONG:
                splitTrimStream(list).forEach(attributes::add);
                break;
            case ATTRIBUTES_FILE:
                attributesFile = new File(list.get(0));
                break;
            case GEM_PATH:
                gemPath = list.get(0);
                break;
            case REQUIRE:
            case REQUIRE_LONG:
                splitTrimStream(list).forEach(requires::add);
                break;
        }
    }

    private Stream<String> splitTrimStream(List<String> list) {
        return list.stream()
                .flatMap(s -> Arrays.stream(s.split("\\s*,\\s*")))
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    void validateOptions() {
        if (baseDir().isEmpty()) {
            printWarning(AsciidocletOptions.BASEDIR + " must be present for includes or file reference features to work properly");
        }

        Optional<File> attrsFile = attributesFile();
        if (attrsFile.isPresent() && !attrsFile.get().canRead()) {
            printWarning("Cannot read attributes file " + attrsFile.get());
        }
    }

    private void printWarning(String message) {
        reporter.print(WARNING, message);
    }

    Optional<File> stylesheet() {
        return Optional.ofNullable(stylesheet);
    }

    Optional<File> baseDir() {
        return Optional.ofNullable(basedir);
    }

    Charset encoding() {
        return encoding;
    }

    List<String> attributes() {
        return attributes;
    }

    Optional<File> attributesFile() {
        if (attributesFile == null) {
            return Optional.empty();
        }
        if (!attributesFile.isAbsolute() && baseDir().isPresent()) {
            return Optional.of(new File(baseDir().get(), attributesFile.getPath()));
        }
        return Optional.of(attributesFile);
    }

    String gemPath() {
        return gemPath;
    }

    List<String> requires() {
        return requires;
    }

}
