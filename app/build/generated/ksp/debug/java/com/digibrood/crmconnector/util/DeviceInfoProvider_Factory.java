package com.digibrood.crmconnector.util;

import android.content.Context;
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
public final class DeviceInfoProvider_Factory implements Factory<DeviceInfoProvider> {
  private final Provider<Context> contextProvider;

  public DeviceInfoProvider_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DeviceInfoProvider get() {
    return newInstance(contextProvider.get());
  }

  public static DeviceInfoProvider_Factory create(Provider<Context> contextProvider) {
    return new DeviceInfoProvider_Factory(contextProvider);
  }

  public static DeviceInfoProvider newInstance(Context context) {
    return new DeviceInfoProvider(context);
  }
}
