package com.lessspring.org.function;

import com.lessspring.org.context.Passthrough;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-11-28 17:01
 */
public class CSupplier<T> extends Passthrough implements Supplier<T> {

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
