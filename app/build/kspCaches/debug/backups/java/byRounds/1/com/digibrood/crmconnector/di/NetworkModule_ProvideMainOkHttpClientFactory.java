package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.remote.interceptor.AuthInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.DynamicBaseUrlInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.HttpsEnforcementInterceptor;
import com.digibrood.crmconnector.data.remote.interceptor.TokenAuthenticator;
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
@QualifierMetadata("com.digibrood.crmconnector.di.MainClient")
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
public final class NetworkModule_ProvideMainOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<HttpsEnforcementInterceptor> httpsProvider;

  private final Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider;

  private final Provider<AuthInterceptor> authProvider;

  private final Provider<TokenAuthenticator> authenticatorProvider;

  private final Provider<HttpLoggingInterceptor> loggingProvider;

  public NetworkModule_ProvideMainOkHttpClientFactory(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider,
      Provider<AuthInterceptor> authProvider, Provider<TokenAuthenticator> authenticatorProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    this.httpsProvider = httpsProvider;
    this.dynamicBaseUrlProvider = dynamicBaseUrlProvider;
    this.authProvider = authProvider;
    this.authenticatorProvider = authenticatorProvider;
    this.loggingProvider = loggingProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideMainOkHttpClient(httpsProvider.get(), dynamicBaseUrlProvider.get(), authProvider.get(), authenticatorProvider.get(), loggingProvider.get());
  }

  public static NetworkModule_ProvideMainOkHttpClientFactory create(
      Provider<HttpsEnforcementInterceptor> httpsProvider,
      Provider<DynamicBaseUrlInterceptor> dynamicBaseUrlProvider,
      Provider<AuthInterceptor> authProvider, Provider<TokenAuthenticator> authenticatorProvider,
      Provider<HttpLoggingInterceptor> loggingProvider) {
    return new NetworkModule_ProvideMainOkHttpClientFactory(httpsProvider, dynamicBaseUrlProvider, authProvider, authenticatorProvider, loggingProvider);
  }

  public static OkHttpClient provideMainOkHttpClient(HttpsEnforcementInterceptor https,
      DynamicBaseUrlInterceptor dynamicBaseUrl, AuthInterceptor auth,
      TokenAuthenticator authenticator, HttpLoggingInterceptor logging) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMainOkHttpClient(https, dynamicBaseUrl, auth, authenticator, logging));
  }
}
