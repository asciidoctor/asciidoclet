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

import jdk.javadoc.doclet.Doclet;

import java.util.List;

/**
 * An `enum` to define options supported by AsciiDoclet.
 */
// TODO: ideally, the `options.adoc` should be generated from comments in this file.
public enum AsciidocletOptions implements Doclet.Option {
    /**
     * Check Doclet Options documentation.
     */
    ENCODING("encoding"),
    /**
     * Check Doclet Options documentation.
     */
    OVERVIEW("overview"),
    /**
     * Check Doclet Options documentation.
     */
    BASEDIR("base-dir"),
    /**
     * Check Doclet Options documentation.
     */
    STYLESHEET("stylesheetfile"),
    /**
     * Check Doclet Options documentation.
     */
    ATTRIBUTE("a"),
    /**
     * Check Doclet Options documentation.
     */
    ATTRIBUTE_LONG("attribute"),
    /**
     * Check Doclet Options documentation.
     */
    INCLUDE_FILTER("asciidoclet-include"),
    /**
     * Check Doclet Options documentation.
     */
    EXCLUDE_FILTER("asciidoclet-exclude"),
    /**
     * Check Doclet Options documentation.
     */
    ATTRIBUTES_FILE("attributes-file"),
    /**
     * Check Doclet Options documentation.
     */
    GEM_PATH("gem-path"),
    /**
     * Check Doclet Options documentation.
     */
    REQUIRE("r"),
    /**
     * Check Doclet Options documentation.
     */
    REQUIRE_LONG("require");
    /**
     * Check Doclet Options documentation.
     */
    private final String name;

    AsciidocletOptions(String name) {
        this.name = name;
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return List.of("--" + name);
    }

    @Override
    public String getParameters() {
        return "<>";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        return true;
    }
}
