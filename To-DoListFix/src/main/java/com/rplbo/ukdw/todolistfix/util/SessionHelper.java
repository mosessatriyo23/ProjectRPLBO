package com.rplbo.ukdw.todolistfix.util;

import java.util.prefs.Preferences;

public class SessionHelper {
    private static int currentUserId = -1;

    public static void setCurrentUser(int userId) {
        currentUserId = userId;
        saveUserId(userId);
    }

    public static int getCurrentUser() {
        return currentUserId;
    }

    public static int getUserId() {
        Preferences prefs = Preferences.userRoot().node("todoApp");
        return prefs.getInt("userId", -1);
    }

    public static void saveUserId(int userId) {
        Preferences prefs = Preferences.userRoot().node("todoApp");
        prefs.putInt("userId", userId);
    }

    public static void clearUserId() {
        Preferences prefs = Preferences.userRoot().node("todoApp");
        prefs.remove("userId");
        currentUserId = -1;
    }
}