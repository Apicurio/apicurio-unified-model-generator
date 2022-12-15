package io.apicurio.datamodels.core.models.wrapper;

public class WrapperImpl<T> implements Wrapper<T> {

    private T value;

    public WrapperImpl() {
    }

    public WrapperImpl(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }
}
