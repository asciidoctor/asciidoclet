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

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import java.io.IOException;

import static javax.tools.JavaFileObject.Kind.SOURCE;
import static javax.tools.StandardLocation.SOURCE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsciidoctorFileManagerTest {

    @Test
    void preservesMissingInputResults() throws IOException {
        StandardJavaFileManager delegate = mock(StandardJavaFileManager.class);
        when(delegate.getJavaFileForInput(SOURCE_PATH, "missing.Type", SOURCE)).thenReturn(null);
        when(delegate.getFileForInput(SOURCE_PATH, "missing", "include.adoc")).thenReturn(null);
        AsciidoctorFileManager fileManager = new AsciidoctorFileManager(null, delegate);

        assertThat(fileManager.getJavaFileForInput(SOURCE_PATH, "missing.Type", SOURCE)).isNull();
        assertThat(fileManager.getFileForInput(SOURCE_PATH, "missing", "include.adoc")).isNull();
    }
}
