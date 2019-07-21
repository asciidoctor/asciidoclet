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
package org.asciidoctor.asciidoclet;

import jdk.javadoc.doclet.Doclet;

import java.util.List;

class OptionProcessor implements Doclet.Option
{
    private final AsciidocletOptions prototype;
    private final DocletOptions collector;

    OptionProcessor( AsciidocletOptions prototype, DocletOptions collector )
    {
        this.prototype = prototype;
        this.collector = collector;
    }

    @Override
    public int getArgumentCount()
    {
        return prototype.getArgumentCount();
    }

    @Override
    public String getDescription()
    {
        return prototype.getDescription();
    }

    @Override
    public Kind getKind()
    {
        return prototype.getKind();
    }

    @Override
    public List<String> getNames()
    {
        return prototype.getNames();
    }

    @Override
    public String getParameters()
    {
        return prototype.getParameters();
    }

    @Override
    public boolean process( String option, List<String> arguments )
    {
        collector.collect( prototype, arguments );
        return true;
    }
}
