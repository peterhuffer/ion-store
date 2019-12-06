package com.connexta.store.adaptors.updated;

import java.util.List;
import java.util.Map.Entry;

public interface UpdateRequest {

  List<Entry<String, StoreResource>> getStoreResources();
}
