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

import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.DocCommentTable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

class LazyDocCommentTableProcessor {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void processComments(DocCommentTable table, Function<Comment, Comment> commentMapper) {
        // table can be non-LazyDocCommentTable instance only for `default constructors` as far as I know now.
        if (table instanceof LazyDocCommentTable) {
            // Use heckin' raw-types because LazyDocCommentTable.Entry has private access, so we
            // cannot statically express its type here.
            //System.err.println("THEN:" + LazyDocCommentTableProcessor.class + ":processComments:" + System.identityHashCode(table));
            Map map;
            Function<Object, Object> converter;
            try {
                Field tableField = LazyDocCommentTable.class.getDeclaredField("table");
                tableField.setAccessible(true);
                map = (Map) tableField.get(table);
                
                Class<?> entryClass = Class.forName("com.sun.tools.javac.parser.LazyDocCommentTable$Entry");
                Constructor<?> ctor = entryClass.getDeclaredConstructor(Comment.class);
                ctor.setAccessible(true);
                Field commentField = entryClass.getDeclaredField("comment");
                commentField.setAccessible(true);
                Function<Object, Comment> fieldGetter = entry -> {
                    try {
                        return (Comment) commentField.get(entry);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                Function<Comment, Object> instantiator = comment -> {
                    try {
                        return ctor.newInstance(comment);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                converter = fieldGetter.andThen(commentMapper).andThen(instantiator);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.replaceAll((tree, entry) -> converter.apply(entry));
        }
    }
}
