package dev.ithundxr.createnumismatics.content.coins;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class LinkedMergingCoinBag extends MergingCoinBag {
    protected abstract int getDelegate();
    protected abstract void setDelegate(int value);

    @Override
    public int getValue() {
        return getDelegate();
    }

    @Override
    protected void setRaw(int value) {
        setDelegate(value);
    }

    public static class FunctionalLinkedMergingCoinBag extends LinkedMergingCoinBag {

        private final Consumer<Integer> setter;
        private final Supplier<Integer> getter;

        public FunctionalLinkedMergingCoinBag(Consumer<Integer> setter, Supplier<Integer> getter) {
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        protected int getDelegate() {
            return getter.get();
        }

        @Override
        protected void setDelegate(int value) {
            setter.accept(value);
        }
    }
}
