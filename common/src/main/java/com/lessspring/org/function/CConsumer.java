package com.lessspring.org.function;

import com.lessspring.org.context.Passthrough;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 16:58
 */
public class CConsumer<T> extends Passthrough implements Consumer<T> {

    private final Consumer<T> source;

    public CConsumer(Consumer<T> source) {
        this.source = source;
    }

    @Override
    public void accept(T t) {
        transfer();
        try {
            source.accept(t);
        } finally {
            clean();
        }
    }

    @Override
    public Consumer<T> andThen(Consumer<? super T> after) {
        transfer();
        try {
            return new CConsumer<>(source.andThen(new CConsumer<>(after)));
        } finally {
            clean();
        }
    }
}
