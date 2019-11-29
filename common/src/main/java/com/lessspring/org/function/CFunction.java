package com.lessspring.org.function;

import com.lessspring.org.context.Passthrough;

import java.util.function.Function;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:56
 */
public class CFunction<T, R> extends Passthrough implements Function<T, R> {

    private final Function<T, R> source;

    public CFunction(Function<T, R> source) {
        this.source = source;
    }

    @Override
    public R apply(T t) {
        transfer();
        try {
            return source.apply(t);
        } finally {
            clean();
        }
    }

    @Override
    public <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        transfer();
        try {
            return new CFunction<>(source.compose(new CFunction<>(before)));
        } finally {
            clean();
        }
    }

    @Override
    public <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        transfer();
        try {
            return new CFunction<>(source.andThen(new CFunction<>(after)));
        } finally {
            clean();
        }
    }
}
