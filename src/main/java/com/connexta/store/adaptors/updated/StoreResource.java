package com.connexta.store.adaptors.updated;

import java.io.IOException;
import java.io.InputStream;

public interface StoreResource {
  String getId();
  String getSetId();
  long getFileSize();
  String getMediaType();
  InputStream getInputStream() throws IOException;
  String getFileName();
  String getGrouping();
}
