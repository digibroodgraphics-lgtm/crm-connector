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
public final class RecordingScanner_Factory implements Factory<RecordingScanner> {
  private final Provider<Context> contextProvider;

  public RecordingScanner_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RecordingScanner get() {
    return newInstance(contextProvider.get());
  }

  public static RecordingScanner_Factory create(Provider<Context> contextProvider) {
    return new RecordingScanner_Factory(contextProvider);
  }

  public static RecordingScanner newInstance(Context context) {
    return new RecordingScanner(context);
  }
}
