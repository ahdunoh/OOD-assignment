package ood.assignment.adminmodule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

/** Writes a complete replacement file via a temporary sibling file. */
final class TxtFileWriter {
    private TxtFileWriter() { }

    static void write(Path target, List<String> lines) throws IOException {
        Path absolute = target.toAbsolutePath().normalize();
        Path folder = absolute.getParent();
        Files.createDirectories(folder);
        Path temp = Files.createTempFile(folder, absolute.getFileName().toString() + ".", ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
