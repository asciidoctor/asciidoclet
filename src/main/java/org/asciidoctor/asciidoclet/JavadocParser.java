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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

class JavadocParser
{
    private static final String[] DELIMITERS = {"====", "////", "```", "----", "....", "--", "____", "****", "|==="};
    private static final Pattern DELIMITER_OR_TAG =
            compile( "(^\\s*)((" + of( DELIMITERS ).map( Pattern::quote ).collect( joining( ")|(" )) + ")|@)", MULTILINE );
    @SuppressWarnings( "unchecked" )
    private static final Map<String,Pattern> DELIMITER_PATTERNS = Map.ofEntries( of( DELIMITERS ).map(
            k -> Map.entry( k, compile( "(^\\s*)" + quote( k ), MULTILINE ) ) ).toArray( Map.Entry[]::new ) );
    private static final Pattern TAG_NAME = compile( "\\G(\\w+)\\s*" );

    static class Tag {
        String tagName;
        String tagText;

        Tag( String tagName, String tagText )
        {
            this.tagName = tagName;
            this.tagText = tagText;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            Tag tag = (Tag) o;
            return tagName.equals( tag.tagName ) && Objects.equals( tagText, tag.tagText );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( tagName, tagText );
        }

        @Override
        public String toString()
        {
            return "Tag{" + "tagName='" + tagName + '\'' + ", tagText='" + tagText + '\'' + '}';
        }
    }

    /**
     * The full comment string, with both text and tags.
     */
    private final String commentString;

    /**
     * Sorted comments with different tags.
     */
    private final List<Tag> tags = new ArrayList<>();

    /**
     * The body of the javadoc text, without the tags.
     */
    private String commentBody;

    /**
     * Create a JavadocParser that will break the comment string to the main comment text, and its tags.
     * This parser is asciidoctor aware, and will avoid parsing tags inside code blocks.
     */
    JavadocParser(String commentString) {
        this.commentString = commentString;
        parseComment( commentString );
    }

    private void parseComment( String commentString )
    {
        // We parse the text through a state machine that roughly looks like this:
        //
        //      javadoc -> body tag*
        //      body -> (text | block)*
        //      block -> delim(1) text $1
        //      delim ->  '====' | '////' | '```' | '----' | '....' | '--' | '++++' | '____' | '****' | '|==='
        //      tag -> tagName (\s+ tagText)?
        //      tagText -> body
        //
        // There is an additional restriction that tags can only start at the beginning of a line,
        // and block delimiters must stand alone on their lines.
        // This restriction removes a lot of otherwise subtle edge cases from the parsing.

        Matcher matcher = DELIMITER_OR_TAG.matcher( commentString );
        int captureSince = 0;
        while ( matcher.find() )
        {
            String group = matcher.group( 2 );
            if ( group.equals("@"))
            {
                int startOfMatch = matcher.start();
                captureComponent( commentString, captureSince, startOfMatch );
                matcher.usePattern( TAG_NAME );
                if ( matcher.find() )
                {
                    Tag tag = new Tag( group + matcher.group( 1 ), null );
                    tags.add( tag );
                    captureSince = matcher.end();
                }
                matcher.usePattern( DELIMITER_OR_TAG );
            }
            else
            {
                matcher.usePattern( DELIMITER_PATTERNS.get( group ) );
                matcher.find();
                matcher.usePattern( DELIMITER_OR_TAG );
            }
        }
        captureComponent( commentString, captureSince, commentString.length() );
    }

    private void captureComponent( String commentString, int captureSince, int endOfCapture )
    {
        String component = commentString.substring( captureSince, endOfCapture ).trim();
        if ( commentBody == null )
        {
            commentBody = component;
        }
        else
        {
            tags.get( tags.size() - 1 ).tagText = component;
        }
    }

    /**
     * Return the text body of the comment, without the tags.
     */
    String getCommentBody() {
        return commentBody;
    }

    /**
     * Return all of the parsed tags.
     */
    List<Tag> tags() {
        return tags;
    }
}
