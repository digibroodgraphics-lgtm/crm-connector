package com.digibrood.crmconnector.data.remote.interceptor;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<SecurePrefs> prefsProvider;

  public AuthInterceptor_Factory(Provider<SecurePrefs> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(prefsProvider.get());
  }

  public static AuthInterceptor_Factory create(Provider<SecurePrefs> prefsProvider) {
    return new AuthInterceptor_Factory(prefsProvider);
  }

  public static AuthInterceptor newInstance(SecurePrefs prefs) {
    return new AuthInterceptor(prefs);
  }
}
