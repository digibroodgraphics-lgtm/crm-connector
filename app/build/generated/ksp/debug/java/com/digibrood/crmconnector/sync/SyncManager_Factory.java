package com.digibrood.crmconnector.sync;

import android.content.Context;
import com.digibrood.crmconnector.util.PermissionManager;
import com.digibrood.crmconnector.worker.SyncScheduler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SyncManager_Factory implements Factory<SyncManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SyncScheduler> schedulerProvider;

  private final Provider<PermissionManager> permissionManagerProvider;

  public SyncManager_Factory(Provider<Context> contextProvider,
      Provider<SyncScheduler> schedulerProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    this.contextProvider = contextProvider;
    this.schedulerProvider = schedulerProvider;
    this.permissionManagerProvider = permissionManagerProvider;
  }

  @Override
  public SyncManager get() {
    return newInstance(contextProvider.get(), schedulerProvider.get(), permissionManagerProvider.get());
  }

  public static SyncManager_Factory create(Provider<Context> contextProvider,
      Provider<SyncScheduler> schedulerProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    return new SyncManager_Factory(contextProvider, schedulerProvider, permissionManagerProvider);
  }

  public static SyncManager newInstance(Context context, SyncScheduler scheduler,
      PermissionManager permissionManager) {
    return new SyncManager(context, scheduler, permissionManager);
  }
}
