package com.digibrood.crmconnector.ui.screens.permissions;

import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider;
import com.digibrood.crmconnector.util.PermissionManager;
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
public final class PermissionsViewModel_Factory implements Factory<PermissionsViewModel> {
  private final Provider<PermissionManager> permissionManagerProvider;

  private final Provider<StartDestinationProvider> startDestinationProvider;

  public PermissionsViewModel_Factory(Provider<PermissionManager> permissionManagerProvider,
      Provider<StartDestinationProvider> startDestinationProvider) {
    this.permissionManagerProvider = permissionManagerProvider;
    this.startDestinationProvider = startDestinationProvider;
  }

  @Override
  public PermissionsViewModel get() {
    return newInstance(permissionManagerProvider.get(), startDestinationProvider.get());
  }

  public static PermissionsViewModel_Factory create(
      Provider<PermissionManager> permissionManagerProvider,
      Provider<StartDestinationProvider> startDestinationProvider) {
    return new PermissionsViewModel_Factory(permissionManagerProvider, startDestinationProvider);
  }

  public static PermissionsViewModel newInstance(PermissionManager permissionManager,
      StartDestinationProvider startDestinationProvider) {
    return new PermissionsViewModel(permissionManager, startDestinationProvider);
  }
}
