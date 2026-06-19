package com.digibrood.crmconnector.data.remote.interceptor;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("com.digibrood.crmconnector.di.RefreshClient")
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
public final class TokenAuthenticator_Factory implements Factory<TokenAuthenticator> {
  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<CrmApiService> refreshApiProvider;

  public TokenAuthenticator_Factory(Provider<SecurePrefs> prefsProvider,
      Provider<CrmApiService> refreshApiProvider) {
    this.prefsProvider = prefsProvider;
    this.refreshApiProvider = refreshApiProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return newInstance(prefsProvider.get(), refreshApiProvider);
  }

  public static TokenAuthenticator_Factory create(Provider<SecurePrefs> prefsProvider,
      Provider<CrmApiService> refreshApiProvider) {
    return new TokenAuthenticator_Factory(prefsProvider, refreshApiProvider);
  }

  public static TokenAuthenticator newInstance(SecurePrefs prefs,
      Provider<CrmApiService> refreshApi) {
    return new TokenAuthenticator(prefs, refreshApi);
  }
}
