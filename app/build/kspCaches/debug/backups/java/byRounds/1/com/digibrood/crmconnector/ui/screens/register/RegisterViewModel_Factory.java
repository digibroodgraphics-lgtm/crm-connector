package com.digibrood.crmconnector.ui.screens.register;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.repository.DeviceRepository;
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<SecurePrefs> prefsProvider;

  public RegisterViewModel_Factory(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<SecurePrefs> prefsProvider) {
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(deviceRepositoryProvider.get(), prefsProvider.get());
  }

  public static RegisterViewModel_Factory create(
      Provider<DeviceRepository> deviceRepositoryProvider, Provider<SecurePrefs> prefsProvider) {
    return new RegisterViewModel_Factory(deviceRepositoryProvider, prefsProvider);
  }

  public static RegisterViewModel newInstance(DeviceRepository deviceRepository,
      SecurePrefs prefs) {
    return new RegisterViewModel(deviceRepository, prefs);
  }
}
