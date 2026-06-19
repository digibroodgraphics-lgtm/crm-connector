package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.remote.interceptor.HttpsEnforcementInterceptor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.digibrood.crmconnector.di.UploadClient")
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
public final class NetworkModule_ProvideUploadOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<HttpsEnforcementInterceptor> httpsProvider;

  private final Provider<HttpLoggingInterceptor> loggingProvider;

  public NetworkModule_ProvideUploadOkHttpClientFactory(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    this.httpsProvider = httpsProvider;
    this.loggingProvider = loggingProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideUploadOkHttpClient(httpsProvider.get(), loggingProvider.get());
  }

  public static NetworkModule_ProvideUploadOkHttpClientFactory create(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    return new NetworkModule_ProvideUploadOkHttpClientFactory(httpsProvider, loggingProvider);
  }

  public static OkHttpClient provideUploadOkHttpClient(HttpsEnforcementInterceptor https,
      HttpLoggingInterceptor logging) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideUploadOkHttpClient(https, logging));
  }
}
