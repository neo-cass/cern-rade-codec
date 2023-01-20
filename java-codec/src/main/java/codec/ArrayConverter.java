package codec;

import types.ArrayContainer;
import types.JavaContainer;

import java.lang.reflect.Array;
import java.util.function.Function;

public class ArrayConverter<T> implements Function<ArrayContainer<T>, T[]> {
    @Override
    public T[] apply(ArrayContainer<T> tArrayContainer) {

        JavaContainer<T>[]  arr = tArrayContainer.getData();
        Object[] newArr = new Object[arr.length];
        for(int i =0; i<arr.length; i++){
            newArr[i] = arr[i].getData();
        }
        return (T[]) newArr;
    }
}
