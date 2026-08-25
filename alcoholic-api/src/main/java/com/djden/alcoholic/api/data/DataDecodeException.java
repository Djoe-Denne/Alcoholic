package com.djden.alcoholic.api.data;

import com.djden.alcoholic.api.PublicApi;

@PublicApi
public final class DataDecodeException extends RuntimeException {
    private final String path;

    public DataDecodeException(String path, String message) {
        super(format(path, message));
        this.path = path == null || path.isBlank() ? "$" : path;
    }

    public DataDecodeException(String path, String message, Throwable cause) {
        super(format(path, message), cause);
        this.path = path == null || path.isBlank() ? "$" : path;
    }

    public String path() {
        return path;
    }

    public static String child(String parent, String name) {
        String root = parent == null || parent.isBlank() ? "$" : parent;
        return root + "/" + name;
    }

    public static String index(String parent, int value) {
        String root = parent == null || parent.isBlank() ? "$" : parent;
        return root + "[" + value + "]";
    }

    private static String format(String path, String message) {
        String resolved = path == null || path.isBlank() ? "$" : path;
        return resolved + ": " + message;
    }
}
