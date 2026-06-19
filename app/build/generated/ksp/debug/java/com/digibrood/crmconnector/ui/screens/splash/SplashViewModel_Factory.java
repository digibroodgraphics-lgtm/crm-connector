package com.digibrood.crmconnector.ui.screens.splash;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.repository.BrandingRepository;
import com.digibrood.crmconnector.data.repository.DeviceRepository;
import com.digibrood.crmconnector.sync.SyncManager;
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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<BrandingRepository> brandingRepositoryProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<StartDestinationProvider> startDestinationProvider;

  private final Provider<SyncManager> syncManagerProvider;

  public SplashViewModel_Factory(Provider<SecurePrefs> prefsProvider,
      Provider<BrandingRepository> brandingRepositoryProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<StartDestinationProvider> startDestinationProvider,
      Provider<SyncManager> syncManagerProvider) {
    this.prefsProvider = prefsProvider;
    this.brandingRepositoryProvider = brandingRepositoryProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.startDestinationProvider = startDestinationProvider;
    this.syncManagerProvider = syncManagerProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(prefsProvider.get(), brandingRepositoryProvider.get(), deviceRepositoryProvider.get(), startDestinationProvider.get(), syncManagerProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<SecurePrefs> prefsProvider,
      Provider<BrandingRepository> brandingRepositoryProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<StartDestinationProvider> startDestinationProvider,
      Provider<SyncManager> syncManagerProvider) {
    return new SplashViewModel_Factory(prefsProvider, brandingRepositoryProvider, deviceRepositoryProvider, startDestinationProvider, syncManagerProvider);
  }

  public static SplashViewModel newInstance(SecurePrefs prefs,
      BrandingRepository brandingRepository, DeviceRepository deviceRepository,
      StartDestinationProvider startDestinationProvider, SyncManager syncManager) {
    return new SplashViewModel(prefs, brandingRepository, deviceRepository, startDestinationProvider, syncManager);
  }
}
