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
public final class CrashReporter_Factory implements Factory<CrashReporter> {
  private final Provider<Context> contextProvider;

  public CrashReporter_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CrashReporter get() {
    return newInstance(contextProvider.get());
  }

  public static CrashReporter_Factory create(Provider<Context> contextProvider) {
    return new CrashReporter_Factory(contextProvider);
  }

  public static CrashReporter newInstance(Context context) {
    return new CrashReporter(context);
  }
}
