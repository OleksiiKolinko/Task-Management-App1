package mate.academy.service;

import java.io.InputStream;

public interface DropboxService {
    String upload(String path, InputStream inputStream);

    String download(String dropboxFileId);

    void delete(String dropboxFileId);
}
