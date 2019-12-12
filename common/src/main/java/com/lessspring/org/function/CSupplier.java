package com.lessspring.org.function;

import com.lessspring.org.context.PassThrough;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-11-28 17:01
 */
public class CSupplier<T> extends PassThrough implements Supplier<T> {

    private final Supplier<T> source;

    public CSupplier(Supplier<T> source) {
        this.source = source;
    }

    @Override
    public T get() {
        transfer();
        try {
            return source.get();
        } finally {
            clean();
        }
    }
}
