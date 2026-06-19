package com.digibrood.crmconnector.di;

import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideRefreshRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<Moshi> moshiProvider;

  public NetworkModule_ProvideRefreshRetrofitFactory(Provider<OkHttpClient> clientProvider,
      Provider<Moshi> moshiProvider) {
    this.clientProvider = clientProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public Retrofit get() {
    return provideRefreshRetrofit(clientProvider.get(), moshiProvider.get());
  }

  public static NetworkModule_ProvideRefreshRetrofitFactory create(
      Provider<OkHttpClient> clientProvider, Provider<Moshi> moshiProvider) {
    return new NetworkModule_ProvideRefreshRetrofitFactory(clientProvider, moshiProvider);
  }

  public static Retrofit provideRefreshRetrofit(OkHttpClient client, Moshi moshi) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideRefreshRetrofit(client, moshi));
  }
}
