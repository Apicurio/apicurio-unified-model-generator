package io.apicurio.datamodels.core.models.wrapper;

public interface Wrapper<T> {

    T getValue();

    void setValue(T value);
}
