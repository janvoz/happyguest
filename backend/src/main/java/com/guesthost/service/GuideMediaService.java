package com.guesthost.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class GuideMediaService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    /** Store a guide media file and return its GridFS ObjectId string. */
    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        try (InputStream inputStream = file.getInputStream()) {
            ObjectId id = gridFsTemplate.store(inputStream, file.getOriginalFilename(), contentType);
            return id.toHexString();
        }
    }

    /** Load a file from GridFS. Returns null if not found. */
    public GridFsResource load(String fileId) {
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));
        if (file == null) {
            return null;
        }
        try {
            return new GridFsResource(file.getMetadata() != null ? String.valueOf(file.getMetadata().get("_contentType")) : "application/octet-stream",
                    gridFsOperations.getResource(file));
        } catch (Exception e) {
            return null;
        }
    }

    public record GridFsResource(String contentType, org.springframework.data.mongodb.gridfs.GridFsResource resource) {}
}
