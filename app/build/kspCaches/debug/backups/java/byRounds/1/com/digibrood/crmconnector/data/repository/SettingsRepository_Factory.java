package com.digibrood.crmconnector.data.repository;

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
public final class SettingsRepository_Factory implements Factory<SettingsRepository> {
  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<SecurePrefs> prefsProvider;

  public SettingsRepository_Factory(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<SecurePrefs> prefsProvider) {
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SettingsRepository get() {
    return newInstance(apiProvider.get(), moshiProvider.get(), prefsProvider.get());
  }

  public static SettingsRepository_Factory create(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<SecurePrefs> prefsProvider) {
    return new SettingsRepository_Factory(apiProvider, moshiProvider, prefsProvider);
  }

  public static SettingsRepository newInstance(CrmApiService api, Moshi moshi, SecurePrefs prefs) {
    return new SettingsRepository(api, moshi, prefs);
  }
}
