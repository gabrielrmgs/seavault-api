package br.dev.irontech.seavault.files.storage;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class LocalFileStorage implements FileStorage {

    @ConfigProperty(name = "seavault.files.local.base-dir")
    String baseDir;

    @Override
    public void store(String storageKey, byte[] content) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar arquivo: " + storageKey, e);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler arquivo: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao remover arquivo: " + storageKey, e);
        }
    }

    private Path resolve(String storageKey) {
        return Path.of(baseDir).resolve(storageKey).normalize();
    }
}
