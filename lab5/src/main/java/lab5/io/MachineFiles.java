package lab5.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class MachineFiles {
    /*
    Проверяет пути к файлам и выполняет безопасную замену файла назначения.
    */
    private MachineFiles() {
        /*
        Закрывает создание экземпляров класса со статическими операциями.
        */
    }

    public static void requireDifferent(Path first, Path second) throws IOException {
        /*
        Проверяет, что пути указывают на разные файлы, включая случай с файловыми ссылками.
        */
        if (first.toAbsolutePath().normalize().equals(second.toAbsolutePath().normalize())
                || (Files.exists(first) && Files.exists(second) && Files.isSameFile(first, second))) {
            throw new IOException("Укажите два разных файла.");
        }
    }

    public static void replace(Path source, Path destination) throws IOException {
        /*
        Копирует источник во временный файл рядом с назначением и заменяет назначение одной операцией.

        Args:
            source: файл, из которого берётся содержимое.
            destination: файл, содержимое которого заменяется.
        */
        requireDifferent(source, destination);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Файл-источник недоступен: " + source);
        }
        Path target = destination.toAbsolutePath().normalize();
        if (target.getParent() == null || Files.isDirectory(target)) {
            throw new IOException("Назначение должно быть файлом: " + destination);
        }
        Path temporary = Files.createTempFile(target.getParent(), ".lab5-", ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
