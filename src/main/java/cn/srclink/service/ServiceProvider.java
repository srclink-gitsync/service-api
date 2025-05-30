package cn.srclink.service;

import java.util.Collection;

public interface ServiceProvider {
    <S> Collection<S> load(Class<S> clazz);
}
