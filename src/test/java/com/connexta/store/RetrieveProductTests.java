/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.AmazonS3ExceptionBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

@SpringBootTest
@AutoConfigureMockMvc
public class RetrieveProductTests {

  private static final String PRODUCT_ID = "341d6c1ce5e0403a99fe86edaed66eea";

  @MockBean private AmazonS3 mockAmazonS3;

  @Inject private MockMvc mockMvc;

  @Value("${s3.bucket}")
  private String s3Bucket;

  @AfterEach
  public void after() {
    verifyNoMoreInteractions(ignoreStubs(mockAmazonS3));
  }

  @Test
  @SuppressWarnings("squid:S2699") // Test case missing assertion
  public void testContextLoads() {}

  @Test
  public void testBadRequest() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/mis/product/    "))
        .andExpect(status().isBadRequest());
  }

  /**
   * @see StoreITests#testRetrieveProductIdNotFound()
   * @see StoreITests#testRetrieveProductWhenS3IsEmpty()
   */
  @Test
  public void testS3KeyDoesNotExist() throws Exception {
    final String key = PRODUCT_ID;
    final AmazonS3ExceptionBuilder amazonS3ExceptionBuilder = new AmazonS3ExceptionBuilder();
    amazonS3ExceptionBuilder.setErrorCode("NoSuchKey");
    amazonS3ExceptionBuilder.setErrorMessage("The specified key does not exist.");
    amazonS3ExceptionBuilder.setStatusCode(404);
    amazonS3ExceptionBuilder.addAdditionalDetail("BucketName", s3Bucket);
    amazonS3ExceptionBuilder.addAdditionalDetail("Resource", "/" + s3Bucket + "/" + key);
    amazonS3ExceptionBuilder.addAdditionalDetail("Key", key);
    when(mockAmazonS3.getObject(
            argThat(
                getObjectRequest ->
                    StringUtils.equals(getObjectRequest.getBucketName(), s3Bucket)
                        && StringUtils.equals(getObjectRequest.getKey(), key))))
        .thenThrow(amazonS3ExceptionBuilder.build());

    // TODO return 404 if key doesn't exist
    assertErrorResponse();
  }

  @Test
  public void testS3BucketDoesNotExist() throws Exception {
    final String key = PRODUCT_ID;
    final AmazonS3ExceptionBuilder amazonS3ExceptionBuilder = new AmazonS3ExceptionBuilder();
    amazonS3ExceptionBuilder.setErrorCode("NoSuchBucket");
    amazonS3ExceptionBuilder.setErrorMessage("The specified bucket does not exist");
    amazonS3ExceptionBuilder.setStatusCode(404);
    amazonS3ExceptionBuilder.addAdditionalDetail("BucketName", s3Bucket);
    amazonS3ExceptionBuilder.addAdditionalDetail("Resource", "/" + s3Bucket + "/" + key);
    amazonS3ExceptionBuilder.addAdditionalDetail("Key", key);
    when(mockAmazonS3.getObject(
            argThat(
                getObjectRequest ->
                    StringUtils.equals(getObjectRequest.getBucketName(), s3Bucket))))
        .thenThrow(amazonS3ExceptionBuilder.build());
    assertErrorResponse();
  }

  /** @see AmazonS3#getObject(GetObjectRequest) */
  @Test
  public void testS3ConstraintsWerentMet() throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(null);
    assertErrorResponse();
  }

  @ParameterizedTest
  @ValueSource(
      classes = {SdkClientException.class, AmazonServiceException.class, RuntimeException.class})
  public void testS3ThrowableTypes(final Class<? extends Throwable> throwableType)
      throws Exception {
    when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(throwableType);
    assertErrorResponse();
  }

  private void assertErrorResponse() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/mis/product/" + PRODUCT_ID)
                .header("Accept-Version", "'0.1.0"))
        .andExpect(status().isInternalServerError());
  }
}
