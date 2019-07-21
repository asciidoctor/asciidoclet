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

import com.sun.tools.javac.parser.Tokens;

class AsciidocComment implements Tokens.Comment
{
    private final String asciidoc;
    private final Tokens.Comment comment;

    AsciidocComment( String asciidoc, Tokens.Comment comment )
    {
        this.asciidoc = asciidoc;
        this.comment = comment;
    }

    @Override
    public String getText()
    {
        return asciidoc;
    }

    @Override
    public int getSourcePos( int index )
    {
        // can we somehow map positions in the asciidoctor back to positions in the source javadoc?
        return comment.getSourcePos( 0 );
    }

    @Override
    public CommentStyle getStyle()
    {
        return comment.getStyle();
    }

    @Override
    public boolean isDeprecated()
    {
        return comment.isDeprecated();
    }
}
