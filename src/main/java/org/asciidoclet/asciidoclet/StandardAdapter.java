/*
 * Copyright 2013-2018 John Ericksen
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
package org.asciidoclet.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

/**
 * Adapter class to use the Standard Javadoc Doclet in a non-static context.
 *
 * @author John Ericksen
 */
public class StandardAdapter {

    public int optionLength(String option) {
        return 0;//Standard.optionLength(option);
    }

    public boolean start(RootDoc rootDoc) {
        return true;//Standard.start(rootDoc);
    }

    public boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return false;//Standard.validOptions(options, errorReporter);
    }
}
