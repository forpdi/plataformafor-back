package org.forpdi.core.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Service
public class StorageService {
	
	@Value("${s3.region}")
	private String s3Region;
	@Value("${s3.credentials.accessKey}")
	private String s3AccessKey;
	@Value("${s3.credentials.secretKey}")
	private String s3SecretKey;
	@Value("${s3.bucket}")
	private String s3Bucket;

	public void uploadFile(InputStream inputStream, String contentType, String fileName)
			throws AmazonServiceException, SdkClientException {
		AmazonS3 s3Client = getS3Client();
		
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        PutObjectRequest request = new PutObjectRequest(s3Bucket, fileName, inputStream, metadata);
        s3Client.putObject(request);
	}
	
	public InputStream retrieveFile(String fileLink) throws IOException {
		AmazonS3 s3Client = getS3Client();
		
		S3Object s3object = s3Client.getObject(s3Bucket, fileLink);
		S3ObjectInputStream s3InputStream = s3object.getObjectContent();
		return s3InputStream;
	}
	
	private AmazonS3 getS3Client() {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
		return AmazonS3ClientBuilder.standard()
			.withRegion(s3Region)
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();
	}
}
