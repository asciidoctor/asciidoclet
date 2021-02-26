package org.asciidoctor.asciidoclet;

import java.io.Serializable;

/**
 * Optional class - copied from Google Guava
 *
 * @param <T>
 */
public abstract class Optional<T> implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final class Present<P> extends Optional<P> {
        private final P reference;
        private static final long serialVersionUID = 0L;

        Present(P reference) {
            this.reference = reference;
        }

        public boolean isPresent() {
            return true;
        }

        public P get() {
            return this.reference;
        }

        public boolean equals(Object object) {
            if (object instanceof Present) {
                Present<?> other = (Present)object;
                return this.reference.equals(other.reference);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return 1502476572 + this.reference.hashCode();
        }

        public String toString() {
            return "Optional.of(" + this.reference + ")";
        }
    }

    public static final class Absent<A> extends Optional<A> {
        static final Absent<Object> INSTANCE = new Absent<Object>();
        private static final long serialVersionUID = 0L;

        static <A> Optional<A> withType() {
            return (Optional<A>) INSTANCE;
        }

        private Absent() {
        }

        public boolean isPresent() {
            return false;
        }

        public A get() {
            throw new IllegalStateException("Optional.get() cannot be called on an absent value");
        }

        public boolean equals(Object object) {
            return object == this;
        }

        public int hashCode() {
            return 2040732332;
        }

        public String toString() {
            return "Optional.absent()";
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }



    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    public static <T> Optional<T> of(T reference) {
        if(reference == null) {
            throw new NullPointerException();
        }
        return new Present(reference);
    }

    public static <T> Optional<T> fromNullable(T nullableReference) {
        return nullableReference == null ? absent() : new Present(nullableReference);
    }

    Optional() {
    }

    public abstract boolean isPresent();

    public abstract T get();

    public abstract boolean equals(Object var1);

    public abstract int hashCode();

    public abstract String toString();
}
