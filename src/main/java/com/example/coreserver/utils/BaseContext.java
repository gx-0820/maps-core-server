package com.example.coreserver.utils;

import com.example.coreserver.entity.User;

public class BaseContext {

    public static ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(User user) {
        threadLocal.set(user);
    }

    public static User getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}

