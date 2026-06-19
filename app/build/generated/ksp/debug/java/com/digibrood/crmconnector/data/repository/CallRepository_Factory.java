package com.digibrood.crmconnector.data.repository;

import com.digibrood.crmconnector.data.local.dao.CallDao;
import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import com.digibrood.crmconnector.util.CallLogReader;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.squareup.moshi.Moshi;
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
public final class CallRepository_Factory implements Factory<CallRepository> {
  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<CallDao> callDaoProvider;

  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<CallLogReader> callLogReaderProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<RecordingRepository> recordingRepositoryProvider;

  public CallRepository_Factory(Provider<CrmApiService> apiProvider, Provider<Moshi> moshiProvider,
      Provider<CallDao> callDaoProvider, Provider<SecurePrefs> prefsProvider,
      Provider<CallLogReader> callLogReaderProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<RecordingRepository> recordingRepositoryProvider) {
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.callDaoProvider = callDaoProvider;
    this.prefsProvider = prefsProvider;
    this.callLogReaderProvider = callLogReaderProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.recordingRepositoryProvider = recordingRepositoryProvider;
  }

  @Override
  public CallRepository get() {
    return newInstance(apiProvider.get(), moshiProvider.get(), callDaoProvider.get(), prefsProvider.get(), callLogReaderProvider.get(), deviceInfoProvider.get(), recordingRepositoryProvider.get());
  }

  public static CallRepository_Factory create(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<CallDao> callDaoProvider,
      Provider<SecurePrefs> prefsProvider, Provider<CallLogReader> callLogReaderProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<RecordingRepository> recordingRepositoryProvider) {
    return new CallRepository_Factory(apiProvider, moshiProvider, callDaoProvider, prefsProvider, callLogReaderProvider, deviceInfoProvider, recordingRepositoryProvider);
  }

  public static CallRepository newInstance(CrmApiService api, Moshi moshi, CallDao callDao,
      SecurePrefs prefs, CallLogReader callLogReader, DeviceInfoProvider deviceInfo,
      RecordingRepository recordingRepository) {
    return new CallRepository(api, moshi, callDao, prefs, callLogReader, deviceInfo, recordingRepository);
  }
}
