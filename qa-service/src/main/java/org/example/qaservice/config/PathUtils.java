package org.example.qaservice.config;

public class PathUtils {
    public static String extractQuestionIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length >= 3 && parts[1].equals("questions")) {
            return parts[2];
        }
        throw new IllegalArgumentException("无效的资源路径格式");
    }
}