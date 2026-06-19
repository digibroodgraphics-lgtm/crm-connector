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
public final class DynamicBaseUrlInterceptor_Factory implements Factory<DynamicBaseUrlInterceptor> {
  private final Provider<SecurePrefs> prefsProvider;

  public DynamicBaseUrlInterceptor_Factory(Provider<SecurePrefs> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public DynamicBaseUrlInterceptor get() {
    return newInstance(prefsProvider.get());
  }

  public static DynamicBaseUrlInterceptor_Factory create(Provider<SecurePrefs> prefsProvider) {
    return new DynamicBaseUrlInterceptor_Factory(prefsProvider);
  }

  public static DynamicBaseUrlInterceptor newInstance(SecurePrefs prefs) {
    return new DynamicBaseUrlInterceptor(prefs);
  }
}
