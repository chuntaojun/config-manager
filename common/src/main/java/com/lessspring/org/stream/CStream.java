package com.lessspring.org.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * 该 Stream 能够支持 {@link com.lessspring.org.context.TraceContext} 透传的
 *
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:25
 */
public class CStream<E> {

    private final Stream<E> source;

    public CStream(Stream<E> source) {
        this.source = source;
    }

    public CStream<E> filter(Predicate<? super E> predicate) {
        return null;
    }
    
    public <R> CStream<R> map(Function<? super E, ? extends R> mapper) {
        return null;
    }
    
    public <R> CStream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
        return null;
    }
    
    public CStream<E> distinct() {
        return null;
    }

    public CStream<E> sorted() {
        return null;
    }

    public CStream<E> sorted(Comparator<? super E> comparator) {
        return null;
    }

    public CStream<E> peek(Consumer<? super E> action) {
        return null;
    }

    public CStream<E> limit(long maxSize) {
        return null;
    }
    
    public CStream<E> skip(long n) {
        return null;
    }
    
    public void forEach(Consumer<? super E> action) {
    }

    public void forEachOrdered(Consumer<? super E> action) {
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <A> A[] toArray(IntFunction<A[]> generator) {
        return null;
    }

    public E reduce(E identity, BinaryOperator<E> accumulator) {
        return null;
    }

    public Optional<E> reduce(BinaryOperator<E> accumulator) {
        return Optional.empty();
    }

    public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
        return null;
    }

    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
        return null;
    }
    
    public <R, A> R collect(Collector<? super E, A, R> collector) {
        return null;
    }

    public Optional<E> min(Comparator<? super E> comparator) {
        return Optional.empty();
    }

    public Optional<E> max(Comparator<? super E> comparator) {
        return Optional.empty();
    }
    
    public long count() {
        return source.count();
    }
    
    public boolean anyMatch(Predicate<? super E> predicate) {
        return false;
    }
    
    public boolean allMatch(Predicate<? super E> predicate) {
        return false;
    }
    
    public boolean noneMatch(Predicate<? super E> predicate) {
        return false;
    }

    public Optional<E> findFirst() {
        return source.findFirst();
    }

    public Optional<E> findAny() {
        return source.findAny();
    }

    public Iterator<E> iterator() {
        return source.iterator();
    }
    
    public boolean isParallel() {
        return source.isParallel();
    }

    public CStream<E> sequential() {
        return null;
    }

    public CStream<E> parallel() {
        return null;
    }

    public CStream<E> unordered() {
        return null;
    }

    public CStream<E> onClose(Runnable closeHandler) {
        return null;
    }
    
    public void close() {
        source.close();
    }
}
