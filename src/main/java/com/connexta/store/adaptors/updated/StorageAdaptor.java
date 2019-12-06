package com.connexta.store.adaptors.updated;

import java.util.List;

public interface StorageAdaptor {

  /**
   *
   * @param storeRequest
   * @throws StorageAdapterException if any {@link StoreResource} in the request failed to save
   */
  void store(StoreRequest storeRequest) throws StorageAdapterException;

  /**
   *
   * @param id
   * @return
   * @throws StorageAdapterException if there was an error retrieving the resource
   * @throws StoreResourceNotFound if the resource does not exist
   */
  StoreResource retrieve(String id) throws StorageAdapterException, StoreResourceNotFound;

  /**
   *
   * @param setId
   * @return
   * @throws StorageAdapterException if there was an error retrieving the resources in the dataset
   * @throws StoreResourceNotFound if no resources exist in the dataset
   */
  List<StoreResource> retrieveSet(String setId) throws StorageAdapterException, StoreResourceNotFound;

  /**
   *
   * @param setId
   * @param grouping
   * @return
   * @throws StorageAdapterException if there was an error retrieving the resources in the dataset
   * @throws StoreResourceNotFound if no resources exist in the dataset
   */
  List<StoreResource> retrieveSetWithGroup(String setId, String grouping) throws StorageAdapterException, StoreResourceNotFound;

  /**
   *
   * @param setId
   * @param newGrouping
   * @throws StorageAdapterException if there was an error updating the
   * @throws StoreResourceNotFound 
   */
  void updateSetGroup(String setId, String newGrouping) throws StorageAdapterException, StoreResourceNotFound;

  /**
   *
   * @param id
   * @throws StorageAdapterException if there was an error deleting the resource
   */
  void delete(String id) throws StorageAdapterException;

  /**
   *
   * @param setId
   * @throws StorageAdapterException if there was an error deleting any resources in the dataset
   */
  void deleteSet(String setId) throws StorageAdapterException;

  /**
   *
   * @param updateRequest
   * @throws StorageAdapterException if there was an error updating any resources in the request
   * @throws StoreResourceNotFound if there was an update requested on a non-existent resource
   */
  void update(UpdateRequest updateRequest) throws StorageAdapterException, StoreResourceNotFound;
}
