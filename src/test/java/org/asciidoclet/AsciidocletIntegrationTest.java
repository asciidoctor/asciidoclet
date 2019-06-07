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
package org.asciidoclet;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AsciidocletIntegrationTest
{
    /**
     * Running this test needs the following JVM argument:
     *      --add-exports jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED
     */
    @Test
    public void testJavadocIntegration() throws Exception
    {
        Method execute = Class.forName( "jdk.javadoc.internal.tool.Main" ).getMethod( "execute", String[].class );
        execute.setAccessible( true );
        execute.invoke( null, (Object) new String[] {
                "--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=asciidoclet",
                "--add-exports=jdk.compiler/com.sun.tools.javac.parser=asciidoclet",
                "--add-exports=jdk.compiler/com.sun.tools.javac.tree=asciidoclet",
                "--module-path", classpath(),
                "--limit-modules", "asciidoclet,java.base,jdk.javadoc,asciidoctorj,guava",
                "--class-path", classpath(),
                "-doclet", "org.asciidoclet.Asciidoclet",
                "--source-path", "src/main/java",
                "-d", "target/javadoc-output",
                "org.asciidoclet",
        } );
    }

    private String classpath()
    {
        return Arrays.stream( System.getProperty( "java.class.path" ).split( ":" ) )
                .filter( s -> !s.contains( "ideaIU" ) ) // Filter out Intellij jar files.
                .collect( Collectors.joining( ":" ) );
    }
}
