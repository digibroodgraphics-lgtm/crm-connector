package com.digibrood.crmconnector.data.repository;

import com.digibrood.crmconnector.data.local.dao.CallDao;
import com.digibrood.crmconnector.data.local.dao.RecordingDao;
import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
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
public final class StatsRepository_Factory implements Factory<StatsRepository> {
  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<CallDao> callDaoProvider;

  private final Provider<RecordingDao> recordingDaoProvider;

  private final Provider<SecurePrefs> prefsProvider;

  public StatsRepository_Factory(Provider<CrmApiService> apiProvider, Provider<Moshi> moshiProvider,
      Provider<CallDao> callDaoProvider, Provider<RecordingDao> recordingDaoProvider,
      Provider<SecurePrefs> prefsProvider) {
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.callDaoProvider = callDaoProvider;
    this.recordingDaoProvider = recordingDaoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public StatsRepository get() {
    return newInstance(apiProvider.get(), moshiProvider.get(), callDaoProvider.get(), recordingDaoProvider.get(), prefsProvider.get());
  }

  public static StatsRepository_Factory create(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<CallDao> callDaoProvider,
      Provider<RecordingDao> recordingDaoProvider, Provider<SecurePrefs> prefsProvider) {
    return new StatsRepository_Factory(apiProvider, moshiProvider, callDaoProvider, recordingDaoProvider, prefsProvider);
  }

  public static StatsRepository newInstance(CrmApiService api, Moshi moshi, CallDao callDao,
      RecordingDao recordingDao, SecurePrefs prefs) {
    return new StatsRepository(api, moshi, callDao, recordingDao, prefs);
  }
}
