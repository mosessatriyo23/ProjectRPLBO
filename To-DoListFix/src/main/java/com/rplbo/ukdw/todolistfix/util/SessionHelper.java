package com.rplbo.ukdw.todolistfix.util;

public class SessionHelper {
    private static int currentUserId;

    public static void setCurrentUser(int idUser) {
        currentUserId = idUser;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }
}
