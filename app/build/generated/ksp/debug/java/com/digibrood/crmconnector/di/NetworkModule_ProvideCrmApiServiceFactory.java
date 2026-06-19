package com.digibrood.crmconnector.di;

import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideCrmApiServiceFactory implements Factory<CrmApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideCrmApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public CrmApiService get() {
    return provideCrmApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideCrmApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideCrmApiServiceFactory(retrofitProvider);
  }

  public static CrmApiService provideCrmApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideCrmApiService(retrofit));
  }
}
