package com.digibrood.crmconnector.ui.navigation;

import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.util.PermissionManager;
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
public final class StartDestinationProvider_Factory implements Factory<StartDestinationProvider> {
  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<PermissionManager> permissionManagerProvider;

  public StartDestinationProvider_Factory(Provider<SecurePrefs> prefsProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    this.prefsProvider = prefsProvider;
    this.permissionManagerProvider = permissionManagerProvider;
  }

  @Override
  public StartDestinationProvider get() {
    return newInstance(prefsProvider.get(), permissionManagerProvider.get());
  }

  public static StartDestinationProvider_Factory create(Provider<SecurePrefs> prefsProvider,
      Provider<PermissionManager> permissionManagerProvider) {
    return new StartDestinationProvider_Factory(prefsProvider, permissionManagerProvider);
  }

  public static StartDestinationProvider newInstance(SecurePrefs prefs,
      PermissionManager permissionManager) {
    return new StartDestinationProvider(prefs, permissionManager);
  }
}
