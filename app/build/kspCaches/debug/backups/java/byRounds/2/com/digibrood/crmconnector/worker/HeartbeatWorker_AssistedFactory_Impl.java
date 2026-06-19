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
public final class HeartbeatWorker_AssistedFactory_Impl implements HeartbeatWorker_AssistedFactory {
  private final HeartbeatWorker_Factory delegateFactory;

  HeartbeatWorker_AssistedFactory_Impl(HeartbeatWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public HeartbeatWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<HeartbeatWorker_AssistedFactory> create(
      HeartbeatWorker_Factory delegateFactory) {
    return InstanceFactory.create(new HeartbeatWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<HeartbeatWorker_AssistedFactory> createFactoryProvider(
      HeartbeatWorker_Factory delegateFactory) {
    return InstanceFactory.create(new HeartbeatWorker_AssistedFactory_Impl(delegateFactory));
  }
}
