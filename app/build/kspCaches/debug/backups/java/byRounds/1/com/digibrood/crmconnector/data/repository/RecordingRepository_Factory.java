package com.digibrood.crmconnector.data.repository;

import com.digibrood.crmconnector.data.local.dao.RecordingDao;
import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.digibrood.crmconnector.util.RecordingScanner;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.digibrood.crmconnector.di.UploadClient")
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
public final class RecordingRepository_Factory implements Factory<RecordingRepository> {
  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<RecordingDao> recordingDaoProvider;

  private final Provider<RecordingScanner> scannerProvider;

  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<OkHttpClient> uploadClientProvider;

  public RecordingRepository_Factory(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<RecordingDao> recordingDaoProvider,
      Provider<RecordingScanner> scannerProvider, Provider<SecurePrefs> prefsProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<OkHttpClient> uploadClientProvider) {
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.recordingDaoProvider = recordingDaoProvider;
    this.scannerProvider = scannerProvider;
    this.prefsProvider = prefsProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.uploadClientProvider = uploadClientProvider;
  }

  @Override
  public RecordingRepository get() {
    return newInstance(apiProvider.get(), moshiProvider.get(), recordingDaoProvider.get(), scannerProvider.get(), prefsProvider.get(), deviceInfoProvider.get(), uploadClientProvider.get());
  }

  public static RecordingRepository_Factory create(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<RecordingDao> recordingDaoProvider,
      Provider<RecordingScanner> scannerProvider, Provider<SecurePrefs> prefsProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<OkHttpClient> uploadClientProvider) {
    return new RecordingRepository_Factory(apiProvider, moshiProvider, recordingDaoProvider, scannerProvider, prefsProvider, deviceInfoProvider, uploadClientProvider);
  }

  public static RecordingRepository newInstance(CrmApiService api, Moshi moshi,
      RecordingDao recordingDao, RecordingScanner scanner, SecurePrefs prefs,
      DeviceInfoProvider deviceInfo, OkHttpClient uploadClient) {
    return new RecordingRepository(api, moshi, recordingDao, scanner, prefs, deviceInfo, uploadClient);
  }
}
