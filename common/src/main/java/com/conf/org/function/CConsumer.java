package com.conf.org.function;

import com.conf.org.context.PassThrough;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-28 16:58
 */
public class CConsumer<T> extends PassThrough implements Consumer<T> {

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
