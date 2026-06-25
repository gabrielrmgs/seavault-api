package br.dev.irontech.seavault.files.storage;

/**
 * Armazenamento de bytes de arquivo. A chave (storageKey) e opaca e unica por arquivo;
 * pode conter '/' para particionar por usuario. O metadado vive no banco, nao aqui.
 */
public interface FileStorage {

    void store(String storageKey, byte[] content);

    byte[] read(String storageKey);

    void delete(String storageKey);
}
