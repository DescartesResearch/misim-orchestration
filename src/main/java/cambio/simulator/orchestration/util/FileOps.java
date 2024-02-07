package cambio.simulator.orchestration.util;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileOps {
    public static void createDirectoryIfDoesntExist(@NonNull Path path) {
        if (path.toString().isEmpty()) {
            System.err.println("Error creating directory: Argument \"path\" must not be an empty path");
        }
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            System.err.println("Error creating directory: " + path + ":\n" + e.getMessage());
        }
    }

    public static List<String> listFileNames(String dir) throws IOException {
        File directory = new File(dir);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Directory does not exist or is not a directory: " + dir);
        }

        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Unable to list files in the directory: " + dir);
        }

        return Arrays.stream(files).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toList());
    }

    public static void copyDirectory(Path src, Path dest) throws IOException {
        copyDirectory(src, dest, Collections.emptyList());
    }

    public static void copyDirectory(Path src, Path dest, List<String> removeFilters) throws IOException {
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> {
                try {
                    Path relativePath = src.relativize(source);
                    Path target = dest.resolve(relativePath.toString());

                    if (!removeFilters.contains(relativePath.getFileName().toString())) {
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(target);
                        } else {
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error copying file: " + source + ": " + e.getMessage());
                }
            });
        }
    }


}
