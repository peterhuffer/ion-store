/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.config;

import com.connexta.store.adaptors.StorageAdaptor;
import com.connexta.store.clients.IndexClient;
import com.connexta.store.service.api.StoreService;
import com.connexta.store.service.impl.StoreServiceImpl;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageManagerConfiguration {

  @Bean
  public StoreService productStorageManager(
      @NotBlank @Value("${endpointUrl.retrieve}") final String retrieveEndpoint,
      @NotNull final StorageAdaptor storageAdapter,
      @NotNull final IndexClient indexClient) {
    return new StoreServiceImpl(retrieveEndpoint, storageAdapter, indexClient);
  }
}
