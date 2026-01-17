package net.pl3x.livemap.util;

import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FileUtil {
    @NotNull
    public static String readString(@NotNull Path path) {
        try {
            return Files.exists(path) ? Files.readString(path) : "";
        } catch (IOException e) {
            Logger.severe("Error reading file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     *
     * @param dir the directory to create
     */
    public static Path createDirs(@NotNull Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir;
    }
}
