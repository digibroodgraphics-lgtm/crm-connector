package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.remote.interceptor.DynamicBaseUrlInterceptor;
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
@QualifierMetadata("com.digibrood.crmconnector.di.RefreshClient")
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
public final class NetworkModule_ProvideRefreshOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<HttpsEnforcementInterceptor> httpsProvider;

  private final Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider;

  private final Provider<HttpLoggingInterceptor> loggingProvider;

  public NetworkModule_ProvideRefreshOkHttpClientFactory(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    this.httpsProvider = httpsProvider;
    this.dynamicBaseUrlProvider = dynamicBaseUrlProvider;
    this.loggingProvider = loggingProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideRefreshOkHttpClient(httpsProvider.get(), dynamicBaseUrlProvider.get(), loggingProvider.get());
  }

  public static NetworkModule_ProvideRefreshOkHttpClientFactory create(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    return new NetworkModule_ProvideRefreshOkHttpClientFactory(httpsProvider, dynamicBaseUrlProvider, loggingProvider);
  }

  public static OkHttpClient provideRefreshOkHttpClient(HttpsEnforcementInterceptor https,
      DynamicBaseUrlInterceptor dynamicBaseUrl, HttpLoggingInterceptor logging) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideRefreshOkHttpClient(https, dynamicBaseUrl, logging));
  }
}
