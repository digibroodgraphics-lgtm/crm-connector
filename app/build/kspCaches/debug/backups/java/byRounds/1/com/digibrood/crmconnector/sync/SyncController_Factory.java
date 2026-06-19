package com.digibrood.crmconnector.sync;

import com.digibrood.crmconnector.data.repository.CallRepository;
import com.digibrood.crmconnector.data.repository.ContactRepository;
import com.digibrood.crmconnector.data.repository.DeviceRepository;
import com.digibrood.crmconnector.data.repository.RecordingRepository;
import com.digibrood.crmconnector.data.repository.SettingsRepository;
import com.digibrood.crmconnector.util.ConnectivityObserver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SyncController_Factory implements Factory<SyncController> {
  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<CallRepository> callRepositoryProvider;

  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<ConnectivityObserver> connectivityProvider;

  public SyncController_Factory(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<ConnectivityObserver> connectivityProvider) {
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.callRepositoryProvider = callRepositoryProvider;
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.connectivityProvider = connectivityProvider;
  }

  @Override
  public SyncController get() {
    return newInstance(deviceRepositoryProvider.get(), settingsRepositoryProvider.get(), callRepositoryProvider.get(), recordingRepositoryProvider.get(), contactRepositoryProvider.get(), connectivityProvider.get());
  }

  public static SyncController_Factory create(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<ConnectivityObserver> connectivityProvider) {
    return new SyncController_Factory(deviceRepositoryProvider, settingsRepositoryProvider, callRepositoryProvider, recordingRepositoryProvider, contactRepositoryProvider, connectivityProvider);
  }

  public static SyncController newInstance(DeviceRepository deviceRepository,
      SettingsRepository settingsRepository, CallRepository callRepository,
      RecordingRepository recordingRepository, ContactRepository contactRepository,
      ConnectivityObserver connectivity) {
    return new SyncController(deviceRepository, settingsRepository, callRepository, recordingRepository, contactRepository, connectivity);
  }
}
