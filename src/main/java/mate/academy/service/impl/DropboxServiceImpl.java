package mate.academy.service.impl;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadBuilder;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.service.DropboxService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DropboxServiceImpl implements DropboxService {
    private final DbxClientV2 client;

    @Override
    public String upload(String path, InputStream inputStream) {
        final UploadBuilder uploadBuilder = client.files().uploadBuilder(path);
        try {
            return uploadBuilder.uploadAndFinish(inputStream).getId();
        } catch (DbxException | IOException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    @Override
    public String download(String dropboxFileId) {
        try {
            return client.files().getTemporaryLink(dropboxFileId).getLink();
        } catch (DbxException e) {
            throw new EntityNotFoundException(
                    "Can't find file on dropbox by file id " + dropboxFileId);
        }
    }

    @Override
    public void delete(String dropboxFileId) {
        try {
            client.files().deleteV2(dropboxFileId);
        } catch (DbxException e) {
            throw new EntityNotFoundException(
                    "Can't find file on dropbox by file id " + dropboxFileId);
        }
    }
}
