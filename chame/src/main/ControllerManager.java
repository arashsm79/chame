package main;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerManager {

    //heterogeneous container
    private static ConcurrentHashMap<Class<?>, Object> controllerHashMap = new ConcurrentHashMap<>();

    public static <T> void putController(Class<T> type, T instance){
        if(type == null)
            return;
        controllerHashMap.put(type, type.cast(instance));

    }

    public static <T> T getController(Class<T> type){
        return type.cast(controllerHashMap.get(type));
    }

    public static <T> boolean containsController(Class<T> type){
        return controllerHashMap.containsKey(type);
    }

}
