package com.digibrood.crmconnector.ui.screens.session;

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
public final class SessionViewModel_Factory implements Factory<SessionViewModel> {
  private final Provider<SecurePrefs> prefsProvider;

  public SessionViewModel_Factory(Provider<SecurePrefs> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SessionViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static SessionViewModel_Factory create(Provider<SecurePrefs> prefsProvider) {
    return new SessionViewModel_Factory(prefsProvider);
  }

  public static SessionViewModel newInstance(SecurePrefs prefs) {
    return new SessionViewModel(prefs);
  }
}
