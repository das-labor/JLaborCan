package org.labor.message;

public interface MessageInputStream<T extends Message> {

    public T read();
}
