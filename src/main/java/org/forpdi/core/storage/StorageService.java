package org.forpdi.core.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
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

	/**
	 * Upload com contentLength especificado (recomendado)
	 */
	public void uploadFile(InputStream inputStream, String contentType, String fileName, long contentLength)
			throws AmazonServiceException, SdkClientException {
		AmazonS3 s3Client = getS3Client();

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentLength);
		metadata.setContentType(contentType);

		PutObjectRequest request = new PutObjectRequest(s3Bucket, fileName, inputStream, metadata);
		s3Client.putObject(request);
	}

	/**
	 * Upload legacy (sem contentLength) — ainda funciona, mas pode causar buffer
	 */
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

	/**
	 * Gera uma URL pré-assinada (GET) para baixar o objeto diretamente do S3,
	 * evitando passar pelo seu endpoint /api/file. Útil para abrir o PDF numa aba.
	 *
	 * @param fileKey chave/objeto no bucket (ex.: "files/123.pdf")
	 * @param ttl     tempo de validade do link (ex.: Duration.ofMinutes(5))
	 * @return URL absoluta (https://...) ou null se falhar
	 */
	public String createPresignedGetUrl(String fileKey, Duration ttl) {
		try {
			AmazonS3 s3Client = getS3Client();

			Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());

			ResponseHeaderOverrides headers = new ResponseHeaderOverrides()
					.withContentType("application/pdf")
					.withContentDisposition("inline; filename=\"relatorio-pdi.pdf\"");

			GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(s3Bucket, fileKey)
					.withMethod(HttpMethod.GET)
					.withExpiration(expiration)
					.withResponseHeaders(headers);

			URL url = s3Client.generatePresignedUrl(req);
			return url.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public String storeFile(File file) throws IOException {
		String key = buildPdfKey();
		try (InputStream in = new FileInputStream(file)) {
			long contentLength = file.length();
			uploadFile(in, "application/pdf", key, contentLength);
			return key;
		}
	}

	private String buildPdfKey() {
		LocalDate d = LocalDate.now();
		return String.format("archives/<uuid>.pdf", d.getYear(), d.getMonthValue(), UUID.randomUUID());
	}
}
