/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.adaptors;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.connexta.store.common.exceptions.StoreException;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

@Slf4j
public class S3StorageAdaptor implements StorageAdaptor {

  private static final String FILE_NAME_METADATA_KEY = "Filename";

  private final String bucket;
  private final AmazonS3 amazonS3;
  private final TransferManager transferManager;

  public S3StorageAdaptor(@NotNull final AmazonS3 amazonS3, @NotBlank final String bucket) {
    this.amazonS3 = amazonS3;
    this.transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    this.bucket = bucket;
  }

  @Override
  public void store(
      @NotNull @Min(1L) @Max(10737418240L) final Long fileSize,
      @NotBlank final String mediaType,
      @NotBlank final String fileName,
      @NotNull final InputStream inputStream,
      @NotBlank final String key)
      throws StoreException {
    // TODO check if id already exists

    final ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(mediaType);
    objectMetadata.setContentLength(fileSize);
    objectMetadata.addUserMetadata(FILE_NAME_METADATA_KEY, fileName);

    log.info("Storing {} in bucket \"{}\" with key \"{}\"", fileName, bucket, key);
    try {
      final Upload upload = transferManager.upload(bucket, key, inputStream, objectMetadata);
      log.info(String.format("Transfer state: %s", upload.getState()));
      upload.waitForCompletion();
      log.info(String.format("Transfer state: %s", upload.getState()));
    } catch (RuntimeException | InterruptedException e) {
      throw new StoreException(
          String.format(
              "Unable to store \"%s\" in bucket \"%s\" with key \"%s\"", fileName, bucket, key),
          e);
    }

    log.info("Successfully stored \"{}\" in bucket \"{}\" with key \"{}\"", fileName, bucket, key);
  }

  /**
   * The caller is responsible for closing the {@link java.io.InputStream} in the returned {@link
   * RetrieveResponse}.
   */
  @Override
  @NotNull
  public RetrieveResponse retrieve(@NotBlank final String key) throws StoreException {
    log.info("Retrieving product in bucket \"{}\" with key \"{}\"", bucket, key);

    S3Object s3Object;
    InputStream productInputStream = null;
    try {
      try {
        s3Object = amazonS3.getObject(new GetObjectRequest(bucket, key));
      } catch (SdkClientException e) {
        throw new StoreException("Unable to retrieve product with key " + key, e);
      }

      final String fileName =
          s3Object.getObjectMetadata().getUserMetaDataOf(FILE_NAME_METADATA_KEY);
      if (StringUtils.isEmpty(fileName)) {
        throw new StoreException(
            String.format(
                "Expected S3 object to have a non-null metadata value for %s",
                FILE_NAME_METADATA_KEY));
      }
      productInputStream = s3Object.getObjectContent();

      return new RetrieveResponse(
          MediaType.valueOf(s3Object.getObjectMetadata().getContentType()),
          productInputStream,
          fileName);
    } catch (Throwable t) {
      if (productInputStream != null) {
        try {
          productInputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", key, e);
        }
      }

      throw t;
    }
  }
}
