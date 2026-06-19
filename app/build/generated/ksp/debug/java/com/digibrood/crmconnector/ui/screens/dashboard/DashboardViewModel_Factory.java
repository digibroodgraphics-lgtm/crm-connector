package com.digibrood.crmconnector.ui.screens.dashboard;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.repository.CallRepository;
import com.digibrood.crmconnector.data.repository.DeviceRepository;
import com.digibrood.crmconnector.data.repository.RecordingRepository;
import com.digibrood.crmconnector.data.repository.SettingsRepository;
import com.digibrood.crmconnector.data.repository.StatsRepository;
import com.digibrood.crmconnector.sync.SyncController;
import com.digibrood.crmconnector.sync.SyncManager;
import com.digibrood.crmconnector.util.CallLogReader;
import com.digibrood.crmconnector.util.ConnectivityObserver;
import com.digibrood.crmconnector.util.CrashReporter;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.digibrood.crmconnector.util.PermissionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<StatsRepository> statsRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<CallRepository> callRepositoryProvider;

  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<ConnectivityObserver> connectivityProvider;

  private final Provider<SyncManager> syncManagerProvider;

  private final Provider<SyncController> syncControllerProvider;

  private final Provider<PermissionManager> permissionManagerProvider;

  private final Provider<CallLogReader> callLogReaderProvider;

  private final Provider<CrashReporter> crashReporterProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<SecurePrefs> prefsProvider;

  public DashboardViewModel_Factory(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<ConnectivityObserver> connectivityProvider,
      Provider<SyncManager> syncManagerProvider, Provider<SyncController> syncControllerProvider,
      Provider<PermissionManager> permissionManagerProvider,
      Provider<CallLogReader> callLogReaderProvider, Provider<CrashReporter> crashReporterProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider, Provider<SecurePrefs> prefsProvider) {
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.statsRepositoryProvider = statsRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.callRepositoryProvider = callRepositoryProvider;
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.connectivityProvider = connectivityProvider;
    this.syncManagerProvider = syncManagerProvider;
    this.syncControllerProvider = syncControllerProvider;
    this.permissionManagerProvider = permissionManagerProvider;
    this.callLogReaderProvider = callLogReaderProvider;
    this.crashReporterProvider = crashReporterProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(deviceRepositoryProvider.get(), statsRepositoryProvider.get(), settingsRepositoryProvider.get(), callRepositoryProvider.get(), recordingRepositoryProvider.get(), connectivityProvider.get(), syncManagerProvider.get(), syncControllerProvider.get(), permissionManagerProvider.get(), callLogReaderProvider.get(), crashReporterProvider.get(), deviceInfoProvider.get(), prefsProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<ConnectivityObserver> connectivityProvider,
      Provider<SyncManager> syncManagerProvider, Provider<SyncController> syncControllerProvider,
      Provider<PermissionManager> permissionManagerProvider,
      Provider<CallLogReader> callLogReaderProvider, Provider<CrashReporter> crashReporterProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider, Provider<SecurePrefs> prefsProvider) {
    return new DashboardViewModel_Factory(deviceRepositoryProvider, statsRepositoryProvider, settingsRepositoryProvider, callRepositoryProvider, recordingRepositoryProvider, connectivityProvider, syncManagerProvider, syncControllerProvider, permissionManagerProvider, callLogReaderProvider, crashReporterProvider, deviceInfoProvider, prefsProvider);
  }

  public static DashboardViewModel newInstance(DeviceRepository deviceRepository,
      StatsRepository statsRepository, SettingsRepository settingsRepository,
      CallRepository callRepository, RecordingRepository recordingRepository,
      ConnectivityObserver connectivity, SyncManager syncManager, SyncController syncController,
      PermissionManager permissionManager, CallLogReader callLogReader, CrashReporter crashReporter,
      DeviceInfoProvider deviceInfo, SecurePrefs prefs) {
    return new DashboardViewModel(deviceRepository, statsRepository, settingsRepository, callRepository, recordingRepository, connectivity, syncManager, syncController, permissionManager, callLogReader, crashReporter, deviceInfo, prefs);
  }
}
