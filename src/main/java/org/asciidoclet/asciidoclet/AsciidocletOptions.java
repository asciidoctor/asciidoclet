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

import jdk.javadoc.doclet.Doclet;

import java.util.List;

public enum AsciidocletOptions implements Doclet.Option
{
    ENCODING( "encoding" ),
    OVERVIEW( "overview" ),
    BASEDIR( "base-dir" ),
    STYLESHEET( "stylesheetfile" ),
    DESTDIR( "d" ),
    ATTRIBUTE( "a" ),
    ATTRIBUTE_LONG( "attribute" ),
    ATTRIBUTES_FILE( "attributes-file" ),
    GEM_PATH( "gem-path" ),
    REQUIRE( "r" ),
    REQUIRE_LONG( "require" );

    private final String name;

    AsciidocletOptions( String name )
    {
        this.name = name;
    }

    @Override
    public int getArgumentCount()
    {
        return 1;
    }

    @Override
    public String getDescription()
    {
        return name;
    }

    @Override
    public Kind getKind()
    {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames()
    {
        return List.of( "--" + name );
    }

    @Override
    public String getParameters()
    {
        return "<>";
    }

    @Override
    public boolean process( String option, List<String> arguments )
    {
        return true;
    }
}
