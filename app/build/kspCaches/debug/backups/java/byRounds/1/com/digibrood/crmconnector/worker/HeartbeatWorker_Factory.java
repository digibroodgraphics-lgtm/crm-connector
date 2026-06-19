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
public final class HeartbeatWorker_Factory {
  private final Provider<SyncController> syncControllerProvider;

  public HeartbeatWorker_Factory(Provider<SyncController> syncControllerProvider) {
    this.syncControllerProvider = syncControllerProvider;
  }

  public HeartbeatWorker get(Context appContext, WorkerParameters params) {
    return newInstance(appContext, params, syncControllerProvider.get());
  }

  public static HeartbeatWorker_Factory create(Provider<SyncController> syncControllerProvider) {
    return new HeartbeatWorker_Factory(syncControllerProvider);
  }

  public static HeartbeatWorker newInstance(Context appContext, WorkerParameters params,
      SyncController syncController) {
    return new HeartbeatWorker(appContext, params, syncController);
  }
}
