package com.fashare.javasugar.annotation.designpattern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface ISubject<T> {
    void add(T observer);

    void remove(T observer);

    void clear();

    T asNotifier();

    abstract class Stub<T> implements ISubject<T> {
        protected final ArrayList<T> mObservers = new ArrayList<>();
        private static Map<Class<?>, ?> mNotifierCache = new HashMap<>();

        public void add(T observer) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }

        public void remove(T observer) {
            if (mObservers.contains(observer)) {
                mObservers.remove(observer);
            }
        }

        public void clear() {
            mObservers.clear();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T asNotifier() {
            Class<T> clazz = getTypeParam();

            if (mNotifierCache.get(clazz) != null) {
                return (T) mNotifierCache.get(clazz);
            }
            T notifier = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    for (T observer : mObservers) {
                        method.invoke(observer, args);
                    }
                    return null;
                }
            });

            ((Map) mNotifierCache).put(clazz, notifier);
            return notifier;
        }

        @SuppressWarnings({"unchecked"})
        private Class<T> getTypeParam() {
            return (Class<T>) getParameterUpperBound(0, ((ParameterizedType) this.getClass().getGenericSuperclass()));
        }

        /**
         * copy from retrofit.Utils
         */
        private static Type getParameterUpperBound(int index, ParameterizedType type) {
            Type[] types = type.getActualTypeArguments();
            if (index < 0 || index >= types.length) {
                throw new IllegalArgumentException(
                        "Index " + index + " not in range [0," + types.length + ") for " + type);
            }
            Type paramType = types[index];
            if (paramType instanceof WildcardType) {
                return ((WildcardType) paramType).getUpperBounds()[0];
            }
            return paramType;
        }
    }
}
