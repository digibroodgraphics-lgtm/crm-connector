package com.digibrood.crmconnector.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RecordingUploadWorker_AssistedFactory_Impl implements RecordingUploadWorker_AssistedFactory {
  private final RecordingUploadWorker_Factory delegateFactory;

  RecordingUploadWorker_AssistedFactory_Impl(RecordingUploadWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public RecordingUploadWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<RecordingUploadWorker_AssistedFactory> create(
      RecordingUploadWorker_Factory delegateFactory) {
    return InstanceFactory.create(new RecordingUploadWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<RecordingUploadWorker_AssistedFactory> createFactoryProvider(
      RecordingUploadWorker_Factory delegateFactory) {
    return InstanceFactory.create(new RecordingUploadWorker_AssistedFactory_Impl(delegateFactory));
  }
}
