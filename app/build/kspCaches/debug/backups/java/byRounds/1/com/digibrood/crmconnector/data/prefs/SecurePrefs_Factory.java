package com.digibrood.crmconnector.data.prefs;

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
public final class SecurePrefs_Factory implements Factory<SecurePrefs> {
  private final Provider<Context> contextProvider;

  public SecurePrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecurePrefs get() {
    return newInstance(contextProvider.get());
  }

  public static SecurePrefs_Factory create(Provider<Context> contextProvider) {
    return new SecurePrefs_Factory(contextProvider);
  }

  public static SecurePrefs newInstance(Context context) {
    return new SecurePrefs(context);
  }
}
