package com.digibrood.crmconnector.ui.screens.login;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.repository.AuthRepository;
import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<StartDestinationProvider> startDestinationProvider;

  private final Provider<SecurePrefs> prefsProvider;

  public LoginViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<StartDestinationProvider> startDestinationProvider,
      Provider<SecurePrefs> prefsProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.startDestinationProvider = startDestinationProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(authRepositoryProvider.get(), startDestinationProvider.get(), prefsProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<StartDestinationProvider> startDestinationProvider,
      Provider<SecurePrefs> prefsProvider) {
    return new LoginViewModel_Factory(authRepositoryProvider, startDestinationProvider, prefsProvider);
  }

  public static LoginViewModel newInstance(AuthRepository authRepository,
      StartDestinationProvider startDestinationProvider, SecurePrefs prefs) {
    return new LoginViewModel(authRepository, startDestinationProvider, prefs);
  }
}
