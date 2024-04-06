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
    static final Function<Object, Comment> COMMENT_FIELD_EXTRACTOR = commentFieldExtractor();
    static final Function<Comment, Object> COMMENT_INSTANTIATOR = commentInstantiator();
    public static final Field LAZY_DOC_COMMENT_TABLE_TABLE_FIELD = getLazyDocCommentTable_TableField();


    @SuppressWarnings({"unchecked", "rawtypes"})
    static void processComments(DocCommentTable table, Function<Comment, Comment> commentMapper) {
        // table can be non-LazyDocCommentTable instance only for `default constructors` as far as I know now.
        if (table instanceof LazyDocCommentTable) {
            // Use heckin' raw-types because LazyDocCommentTable.Entry has private access, so we
            // cannot statically express its type here.
            //System.err.println("THEN:" + LazyDocCommentTableProcessor.class + ":processComments:" + System.identityHashCode(table));
            Map map = tableFieldValueOf(table);
            Function<Object, Object> converter = COMMENT_FIELD_EXTRACTOR.andThen(commentMapper).andThen(COMMENT_INSTANTIATOR);
            map.replaceAll((tree, entry) -> converter.apply(entry));
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map tableFieldValueOf(DocCommentTable table) {
        try {
            return (Map) LAZY_DOC_COMMENT_TABLE_TABLE_FIELD.get(table);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Function<Object, Comment> commentFieldExtractor() {
        Field entryClassCommentField = getLazyDocCommentTable$EntryClassCommentField(getLazyDocCommentTable$EntryClass());
        return entry -> {
            try {
                return (Comment) entryClassCommentField.get(entry);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Function<Comment, Object> commentInstantiator() {
        @SuppressWarnings("rawtypes") Constructor entryClassConstructor = getLazyDocCommentTable$EntryClassConstructor();
        return comment -> {
            try {
                return entryClassConstructor.newInstance(comment);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Field getLazyDocCommentTable$EntryClassCommentField(Class<?> entryClass) {
        try {
            Field commentField = entryClass.getDeclaredField("comment");
            commentField.setAccessible(true);
            return commentField;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Constructor<?> getLazyDocCommentTable$EntryClassConstructor() {
        try {
            Constructor<?> ctor = getLazyDocCommentTable$EntryClass().getDeclaredConstructor(Comment.class);
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getLazyDocCommentTable$EntryClass() {
        try {
            return Class.forName("com.sun.tools.javac.parser.LazyDocCommentTable$Entry");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getLazyDocCommentTable_TableField() {
        try {
            Field tableField = LazyDocCommentTable.class.getDeclaredField("table");
            tableField.setAccessible(true);
            return tableField;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
