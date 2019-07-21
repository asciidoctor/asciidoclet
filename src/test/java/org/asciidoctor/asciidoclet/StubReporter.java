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

import com.sun.source.util.DocTreePath;
import jdk.javadoc.doclet.Reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import static org.junit.Assert.fail;

public class StubReporter implements Reporter
{
    private List<List<Object>> calls = new ArrayList<>();

    @Override
    public void print( Diagnostic.Kind kind, String msg )
    {
        calls.add( List.of( kind, msg ) );
    }

    @Override
    public void print( Diagnostic.Kind kind, DocTreePath path, String msg )
    {
        calls.add( List.of( kind, path, msg ) );
    }

    @Override
    public void print( Diagnostic.Kind kind, Element e, String msg )
    {
        calls.add( List.of( kind, e, msg ) );
    }

    void assertNoMoreInteractions()
    {
        if ( !calls.isEmpty() )
        {
            String callsString = calls.stream().map( Object::toString ).collect( Collectors.joining( "\n\t", "\n\t", "" ) );
            fail( "Expected to not have any print calls, but got the following: " + callsString );
        }
    }

    List<Object> pullCall()
    {
        return calls.remove( 0 );
    }
}
