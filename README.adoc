= Asciidoclet
John Ericksen <https://github.com/johncarl81[@johncarl81]>; Ben Evans <https://github.com/benevans[@benevans]>
:description: This is a guide for setting up and using the Asciidoclet project. Asciidoclet is a Javadoc Doclet based on Asciidoctor that lets you write Javadoc in the AsciiDoc syntax.
:keywords: Asciidoclet, AsciiDoc, Asciidoctor, syntax, Javadoc, Doclet, reference
:idprefix:
:idseparator: -
:source-language: java
ifdef::env-browser[]
:sectanchors:
:source-highlighter: highlight.js
:icons: font
endif::[]
ifdef::env-github,env-browser[]
:toc: preamble
endif::[]
ifdef::env-github[]
:badges:
:!toc-title:
endif::[]
// Refs
:asciidoclet-version: 1.5.6
:asciidoclet-src-ref: https://github.com/asciidoctor/asciidoclet
:asciidoclet-javadoc-ref: https://www.javadoc.io/doc/org.asciidoctor/asciidoclet/{asciidoclet-version}
:asciidoclet-release-ref: https://asciidoctor.org/news/2014/09/09/asciidoclet-1.5.0-released/
:asciidoc-ref: https://asciidoc.org
:asciidoctor-java-ref: https://asciidoctor.org/docs/install-and-use-asciidoctor-java-integration/
:asciidoclet-issues-ref: https://github.com/asciidoctor/asciidoclet/issues
:asciidoctor-src-ref: https://github.com/asciidoctor/asciidoctor
:asciidoctor-java-src-ref: https://github.com/asciidoctor/asciidoctor-java-integration
:discuss-ref: https://discuss.asciidoctor.org/

ifdef::badges[]
image:https://img.shields.io/travis/asciidoctor/asciidoclet/master.svg["Build Status", link="https://travis-ci.org/asciidoctor/asciidoclet"]
image:https://img.shields.io/badge/javadoc.io-{asciidoclet-version}-blue.svg[Javadoc, link={asciidoclet-javadoc-ref}]
endif::[]

{asciidoclet-src-ref}[Asciidoclet] is a Javadoc Doclet based on Asciidoctor that lets you write Javadoc in the AsciiDoc syntax.

== Introduction

Traditionally, Javadocs have mixed minor markup with HTML which, if you're writing for HTML Javadoc output, becomes unreadable and hard to write over time.
This is where lightweight markup languages like {asciidoc-ref}[AsciiDoc] thrive.
AsciiDoc straddles the line between readable markup and beautifully rendered content.

Asciidoclet incorporates an AsciiDoc renderer (Asciidoctor via the {asciidoctor-java-ref}[Asciidoctor Java integration] library) into a simple Doclet that enables AsciiDoc formatting within Javadoc comments and tags.

== Example

Here's an example of a class with traditional Javadoc.

[source]
.A Java class with traditional Javadoc
----
/**
 * <h1>Asciidoclet</h1>
 *
 * <p>Sample comments that include {@code source code}.</p>
 *
 * <pre>{@code
 * public class Asciidoclet extends Doclet {
 *     private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
 *
 *     {@literal @}SuppressWarnings("UnusedDeclaration")
 *     public static boolean start(RootDoc rootDoc) {
 *         new Asciidoclet().render(rootDoc);
 *         return Standard.start(rootDoc);
 *     }
 * }
 * }</pre>
 *
 * @author <a href="https://github.com/johncarl81">John Ericksen</a>
 */
public class Asciidoclet extends Doclet {
}
----

This is the same class with Asciidoclet.

[source]
.A Java class with Asciidoclet Javadoc
----
/**
 * = Asciidoclet
 *
 * Sample comments that include `source code`.
 *
 * [source,java]
 * --
 * public class Asciidoclet extends Doclet {
 *     private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
 *
 *     @SuppressWarnings("UnusedDeclaration")
 *     public static boolean start(RootDoc rootDoc) {
 *         new Asciidoclet().render(rootDoc);
 *         return Standard.start(rootDoc);
 *     }
 * }
 * --
 *
 * @author https://github.com/johncarl81[John Ericksen]
 */
public class Asciidoclet extends Doclet {
}
----

The result is readable source and beautifully rendered Javadocs, the best of both worlds!

// tag::usage[]
== Usage

Run Javadoc with the `org.asciidoctor.Asciidoclet` doclet class.
Some examples for common build systems are shown below.
See <<doclet-options>> for supported options.

=== Maven

Asciidoclet may be used via a `maven-javadoc-plugin` doclet:

[source,xml]
----
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>2.9</version>
    <configuration>
        <source>1.7</source>
        <doclet>org.asciidoctor.Asciidoclet</doclet>
        <docletArtifact>
            <groupId>org.asciidoctor</groupId>
            <artifactId>asciidoclet</artifactId>
            <version>${asciidoclet.version}</version>
        </docletArtifact>
        <overview>src/main/java/overview.adoc</overview>
        <additionalparam>
          --base-dir ${project.basedir}
          --attribute "name=${project.name}"
          --attribute "version=${project.version}"
          --attribute "title-link=https://example.com[${project.name} ${project.version}]"
        </additionalparam>
    </configuration>
</plugin>
----

=== Gradle

Asciidoclet may be used via a doclet in the `Javadoc` task:

[source,groovy]
----
configurations {
    asciidoclet
}

dependencies {
    asciidoclet 'org.asciidoctor:asciidoclet:1.+'
}

javadoc {
    options.docletpath = configurations.asciidoclet.files.asType(List)
    options.doclet = 'org.asciidoctor.Asciidoclet'
    options.overview = "src/main/java/overview.adoc"
    options.addStringOption "-base-dir", "${projectDir}" // <1>
    options.addStringOption "-attribute", // <2>
            "name=${project.name}," +
            "version=${project.version}," +
            "title-link=https://example.com[${project.name} ${project.version}]")
}
----
<1> Option names passed to Gradle's `javadoc` task must omit the leading "-", so here "-base-dir" means "--base-dir".
See <<doclet-options>> below.
<2> Gradle's `javadoc` task does not allow multiple occurrences of the same option.
Multiple attributes can be specified in a single string, separated by commas.

=== Ant
// Some of us still use Ant, alright?!
Asciidoclet may be used via a doclet element in Ant's `javadoc` task:

[source,xml]
----
<javadoc destdir="target/javadoc"
         sourcepath="src"
         overview="src/overview.adoc">
  <doclet name="org.asciidoctor.Asciidoclet" pathref="asciidoclet.classpath"> <!--1-->
    <param name="--base-dir" value="${basedir}"/>
    <param name="--attribute" value="name=${ant.project.name}"/>
    <param name="--attribute" value="version=${version}"/>
    <param name="--attribute" value="title-link=https://example.com[${ant.project.name} ${version}]"/>
  </doclet>
</javadoc>
----

<1> Assumes a path reference has been defined for Asciidoclet and its dependencies, e.g.
using https://ant.apache.org/ivy/[Ivy] or similar.

=== Doclet Options
// tag::doclet-options[]

--base-dir <dir>::
Sets the base directory that will be used to resolve relative path names in Asciidoc `include::` directives.
This should be set to the project's root directory.

-a, --attribute "name[=value], ..."::
Sets https://asciidoctor.org/docs/user-manual/#attributes[document attributes^] that will be expanded in Javadoc comments.
The argument is a string containing a single attribute, or multiple attributes separated by commas.
+
This option may be used more than once, for example: `-a name=foo -a version=1`.
+
Attributes use the same syntax as Asciidoctor command-line attributes:
+
--
* `name` sets the attribute (with an empty value)
* `name=value` assigns `value` to the attribute. Occurrences of `\{name}` in the Javadoc will be replaced by this value.
* `name=value@` assigns `value` to the attribute, unless the attribute is defined in the attributes file or Javadoc.
* `name!` unsets the attribute.
--
+
The document attribute `javadoc` is set automatically by the doclet.
This can be used for conditionally selecting content when using the same Asciidoc file for Javadoc and other documentation.

--attributes-file <file>::
Reads https://asciidoctor.org/docs/user-manual/#attributes[document attributes^] from an Asciidoc file.
The attributes will be expanded in Javadoc comments.
+
If `<file>` is a relative path name, it is assumed to be relative to the `--base-dir` directory.
+
Attributes set by the `-a`/`--attribute` option take precedence over those in the attributes file.

-r, --require <library>,...::
Make the specified RubyGems library available to Asciidoctor's JRuby runtime, for example `-r asciidoctor-diagram`.
+
This option may be specified more than once.
Alternatively multiple library names may be specified in a single argument, separated by commas.

--gem-path <path>::
Sets the `GEM_PATH` for Asciidoctor's JRuby runtime.
This option is only needed when using the `--require` option to load additional gems on the `GEM_PATH`.

-overview <file>::
Overview documentation can be generated from an Asciidoc file using the standard `-overview` option.
Files matching [x-]`*.adoc`, [x-]`*.ad`, [x-]`*.asciidoc` or [x-]`*.txt` are processed by Asciidoclet.
Other files are assumed to be HTML and will be processed by the standard doclet.

--asciidoclet-include <filter>::
--asciidoclet-exclude <filter>::
Explicitly include or exclude classes from being processed as Asciidoc comments by ant-style path matching (see https://github.com/azagniotov/ant-style-path-matcher[ant-style-path-matcher]).
If `--asciidoclet-include` is specified, only classes and packages matching the include filter are processed.
Likewise, if `--include` is unspecified, all classes are processed.
If `--asciidoclet-exclude` is specified, classes specifically matching the filter are not processed.
`--asciidoclet-include` and `--asciidoclet-exclude` can be mixed.
In addition, classes excluded with `--asciidoclet-exclude` or not matching a specified `--asciidoclet-include` may be included by annotating the class level javadoc with `@asciidoclet`.
Doing so allows the ability to write one class at a time while respecting refactors.
This feature allows the migration of documentation from HTML to Asciidoc in a piecemeal way

// end::doclet-options[]
// end::usage[]

=== Log Warning

Currently there is a intermittent benign warning message that is emitted during a run of Asciidoclet stating the following:

....
WARN: tilt autoloading 'tilt/haml' in a non thread-safe way; explicit require 'tilt/haml' suggested.
....

Unfortunately, until the underlying library removes this warning message, it will be logged during the build.

== Additional Features

Make sure to see {asciidoclet-release-ref}[Asciidoclet 1.5.0 Release Notes] for additional features not documented here.

== Resources and help

For more information:

* {asciidoclet-release-ref}[Asciidoclet 1.5.0 Release Notes]
* {asciidoclet-src-ref}[Asciidoclet Source Code]
* {asciidoclet-javadoc-ref}[Asciidoclet JavaDoc]
* {asciidoclet-issues-ref}[Asciidoclet Issue Tracker]
* {asciidoctor-src-ref}[Asciidoctor Source Code]
* {asciidoctor-java-src-ref}[Asciidoctor Java Integration Source Code]

If you have questions or would like to help develop this project, please join the {discuss-ref}[Asciidoctor discussion list].

ifndef::env-site[]
== Powered by Asciidoclet

We have a <<src/docs/asciidoc/asciidoclet-powered.adoc#,Powered by Asciidoclet>> page.
If you have an example of nifty JavaDoc powered by Asciidoclet, please send us a pull request.
endif::[]

== License

....
Copyright (C) 2013-2015 John Ericksen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
....
