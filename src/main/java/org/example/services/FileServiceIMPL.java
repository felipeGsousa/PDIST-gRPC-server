package org.example.services;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoDatabase;
import org.example.grpc.FileDTO;
import org.example.grpc.FileServiceGrpc;
import org.example.grpc.GetFileRequest;
import org.example.grpc.UploadFileResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FileServiceIMPL extends FileServiceGrpc.FileServiceImplBase {

    private final GridFSBucket gridFSBucket;

    public FileServiceIMPL() {
        String mongoUri = "mongodb+srv://Felipe:09BTFhsZwQ0NKcDP@cluster0.ibf1r8y.mongodb.net/PDIST-DB-FILES?retryWrites=true&w=majority&appName=Cluster0";
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase("PDIST-DB-FILES");
        this.gridFSBucket = GridFSBuckets.create(database);
    }

    @Override
    public void uploadFile(FileDTO request, StreamObserver<UploadFileResponse> responseObserver) {
        try {

            String userId = request.getUserId();
            String fileName = request.getFilename();
            String contentType = request.getContentType();
            byte[] fileData = request.getData().toByteArray();

            Document metadata = new Document();
            metadata.put("userId", userId);
            metadata.put("contentType", contentType);

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(metadata);

            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
            ObjectId fileId = gridFSBucket.uploadFromStream(fileName, fileInputStream, options);

            UploadFileResponse response = UploadFileResponse.newBuilder()
                    .setId(fileId.toHexString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getFileById(GetFileRequest request, StreamObserver<FileDTO> responseObserver) {
        String id = request.getId();

        GridFSFile gridFSFile = gridFSBucket.find(new Document("_id", new ObjectId(id))).first();

        if (gridFSFile != null) {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

            Document metadata = gridFSFile.getMetadata();
            String contentType = "application/octet-stream";

            if (metadata != null && metadata.containsKey("contentType")) {
                contentType = metadata.getString("contentType");
            } else if (metadata != null && metadata.containsKey("_contentType")) {
                contentType = metadata.getString("_contentType");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = downloadStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            downloadStream.close();

            byte[] fileData = outputStream.toByteArray();

            FileDTO fileDTO = FileDTO.newBuilder()
                    .setId(id)
                    .setFilename(gridFSFile.getFilename())
                    .setContentType(contentType)
                    .setData(com.google.protobuf.ByteString.copyFrom(fileData))
                    .build();

            responseObserver.onNext(fileDTO);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new Exception("File not found with id: " + id));
        }
        responseObserver.onCompleted();
    }
}