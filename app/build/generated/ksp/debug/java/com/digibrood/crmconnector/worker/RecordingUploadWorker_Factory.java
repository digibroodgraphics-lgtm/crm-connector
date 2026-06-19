package com.digibrood.crmconnector.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.digibrood.crmconnector.sync.SyncController;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class RecordingUploadWorker_Factory {
  private final Provider<SyncController> syncControllerProvider;

  public RecordingUploadWorker_Factory(Provider<SyncController> syncControllerProvider) {
    this.syncControllerProvider = syncControllerProvider;
  }

  public RecordingUploadWorker get(Context appContext, WorkerParameters params) {
    return newInstance(appContext, params, syncControllerProvider.get());
  }

  public static RecordingUploadWorker_Factory create(
      Provider<SyncController> syncControllerProvider) {
    return new RecordingUploadWorker_Factory(syncControllerProvider);
  }

  public static RecordingUploadWorker newInstance(Context appContext, WorkerParameters params,
      SyncController syncController) {
    return new RecordingUploadWorker(appContext, params, syncController);
  }
}
