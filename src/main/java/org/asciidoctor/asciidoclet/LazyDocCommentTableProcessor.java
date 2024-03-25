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
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

class LazyDocCommentTableProcessor {

    @SuppressWarnings("unchecked")
    static DocCommentTable processComments(JCTree jcTree, DocCommentTable table, Function<Comment, Comment> commentMapper) {
        if (false && table instanceof LazyDocCommentTable) {
            // Use heckin' raw-types because LazyDocCommentTable.Entry has private access, so we
            // cannot statically express its type here.
            System.err.println("THEN:" + LazyDocCommentTableProcessor.class + ":processComments:" + System.identityHashCode(table));
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
            return table;
        } else {
            System.err.println("ELSE:" + LazyDocCommentTableProcessor.class + ":processComments:" + System.identityHashCode(table));
            DocCommentTable t = new DocCommentTable() {
                @Override
                public boolean hasComment(JCTree tree) {
                    System.err.println(this + ":hasComment:" + System.identityHashCode(tree));
                    return table.hasComment(tree);
                }

                @Override
                public Comment getComment(JCTree tree) {
                    System.err.println(this + ":getComment:" + System.identityHashCode(tree));
                    return hasComment(tree) ? table.getComment(tree) : null;
                }

                @Override
                public String getCommentText(JCTree tree) {
                    System.err.println(this + ":getCommentText:" + System.identityHashCode(tree));
                    Comment ret = getComment(tree);
                    return ret == null ? "HELLO, WORLD" : ret.getText();
                }

                @Override
                public DCTree.DCDocComment getCommentTree(JCTree tree) {
                    System.err.println(this + ":getCommentTree:" + System.identityHashCode(tree));
                    return table.getCommentTree(jcTree);
                }

                @Override
                public void putComment(JCTree tree, Comment c) {
                    System.err.println(this + ":putComment:" + System.identityHashCode(tree));
                    table.putComment(tree, c);
                }
            };
            return t;
        }
    }
}
