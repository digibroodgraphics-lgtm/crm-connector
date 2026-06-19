package com.digibrood.crmconnector.service;

import com.digibrood.crmconnector.data.repository.DeviceRepository;
import com.digibrood.crmconnector.sync.SyncController;
import com.digibrood.crmconnector.util.ConnectivityObserver;
import com.digibrood.crmconnector.worker.SyncScheduler;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SyncForegroundService_MembersInjector implements MembersInjector<SyncForegroundService> {
  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<SyncController> syncControllerProvider;

  private final Provider<ConnectivityObserver> connectivityProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<SyncScheduler> schedulerProvider;

  public SyncForegroundService_MembersInjector(
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<SyncController> syncControllerProvider,
      Provider<ConnectivityObserver> connectivityProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<SyncScheduler> schedulerProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
    this.syncControllerProvider = syncControllerProvider;
    this.connectivityProvider = connectivityProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.schedulerProvider = schedulerProvider;
  }

  public static MembersInjector<SyncForegroundService> create(
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<SyncController> syncControllerProvider,
      Provider<ConnectivityObserver> connectivityProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<SyncScheduler> schedulerProvider) {
    return new SyncForegroundService_MembersInjector(notificationHelperProvider, syncControllerProvider, connectivityProvider, deviceRepositoryProvider, schedulerProvider);
  }

  @Override
  public void injectMembers(SyncForegroundService instance) {
    injectNotificationHelper(instance, notificationHelperProvider.get());
    injectSyncController(instance, syncControllerProvider.get());
    injectConnectivity(instance, connectivityProvider.get());
    injectDeviceRepository(instance, deviceRepositoryProvider.get());
    injectScheduler(instance, schedulerProvider.get());
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.service.SyncForegroundService.notificationHelper")
  public static void injectNotificationHelper(SyncForegroundService instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.service.SyncForegroundService.syncController")
  public static void injectSyncController(SyncForegroundService instance,
      SyncController syncController) {
    instance.syncController = syncController;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.service.SyncForegroundService.connectivity")
  public static void injectConnectivity(SyncForegroundService instance,
      ConnectivityObserver connectivity) {
    instance.connectivity = connectivity;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.service.SyncForegroundService.deviceRepository")
  public static void injectDeviceRepository(SyncForegroundService instance,
      DeviceRepository deviceRepository) {
    instance.deviceRepository = deviceRepository;
  }

  @InjectedFieldSignature("com.digibrood.crmconnector.service.SyncForegroundService.scheduler")
  public static void injectScheduler(SyncForegroundService instance, SyncScheduler scheduler) {
    instance.scheduler = scheduler;
  }
}
