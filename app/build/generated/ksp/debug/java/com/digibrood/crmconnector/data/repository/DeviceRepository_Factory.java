package com.digibrood.crmconnector.data.repository;

import android.content.Context;
import com.digibrood.crmconnector.data.prefs.SecurePrefs;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import com.digibrood.crmconnector.util.ConnectivityObserver;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DeviceRepository_Factory implements Factory<DeviceRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<SecurePrefs> prefsProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<ConnectivityObserver> connectivityProvider;

  public DeviceRepository_Factory(Provider<Context> contextProvider,
      Provider<CrmApiService> apiProvider, Provider<Moshi> moshiProvider,
      Provider<SecurePrefs> prefsProvider, Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<ConnectivityObserver> connectivityProvider) {
    this.contextProvider = contextProvider;
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.prefsProvider = prefsProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.connectivityProvider = connectivityProvider;
  }

  @Override
  public DeviceRepository get() {
    return newInstance(contextProvider.get(), apiProvider.get(), moshiProvider.get(), prefsProvider.get(), deviceInfoProvider.get(), connectivityProvider.get());
  }

  public static DeviceRepository_Factory create(Provider<Context> contextProvider,
      Provider<CrmApiService> apiProvider, Provider<Moshi> moshiProvider,
      Provider<SecurePrefs> prefsProvider, Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<ConnectivityObserver> connectivityProvider) {
    return new DeviceRepository_Factory(contextProvider, apiProvider, moshiProvider, prefsProvider, deviceInfoProvider, connectivityProvider);
  }

  public static DeviceRepository newInstance(Context context, CrmApiService api, Moshi moshi,
      SecurePrefs prefs, DeviceInfoProvider deviceInfo, ConnectivityObserver connectivity) {
    return new DeviceRepository(context, api, moshi, prefs, deviceInfo, connectivity);
  }
}
