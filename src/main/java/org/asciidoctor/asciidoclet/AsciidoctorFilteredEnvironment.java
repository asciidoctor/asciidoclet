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

import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.internal.tool.DocEnvImpl;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

/**
 * An operating environment defined for AsciiDoclet.
 */
public class AsciidoctorFilteredEnvironment
        extends DocEnvImpl
        implements DocletEnvironment, AutoCloseable {

    private final StandardJavaFileManager fileManager;
    private final AsciiDocTrees asciiDocTrees;

    AsciidoctorFilteredEnvironment(DocletEnvironment environment, AsciidoctorConverter converter) {
        super(((DocEnvImpl) environment).toolEnv, ((DocEnvImpl) environment).etable);
        this.fileManager = new AsciidoctorFileManager(converter, (StandardJavaFileManager) environment.getJavaFileManager());
        this.asciiDocTrees = new AsciiDocTrees(converter, fileManager, environment.getDocTrees());
    }

    @Override
    public JavaFileManager getJavaFileManager() {
        return fileManager;
    }

    @Override
    public DocTrees getDocTrees() {
        return asciiDocTrees;
    }

    @Override
    public void close() {
    }
}
