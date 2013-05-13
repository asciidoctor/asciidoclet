package org.asciidoclet;

import com.sun.javadoc.Tag;

public interface TagRenderer{

    void render(Tag tag, StringBuilder buffer);
}