package com.digibrood.crmconnector.data.repository;

import com.digibrood.crmconnector.data.local.dao.RemarkDao;
import com.digibrood.crmconnector.data.remote.api.CrmApiService;
import com.digibrood.crmconnector.util.ContactReader;
import com.digibrood.crmconnector.util.DeviceInfoProvider;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class ContactRepository_Factory implements Factory<ContactRepository> {
  private final Provider<CrmApiService> apiProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<RemarkDao> remarkDaoProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<ContactReader> contactReaderProvider;

  public ContactRepository_Factory(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<RemarkDao> remarkDaoProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<ContactReader> contactReaderProvider) {
    this.apiProvider = apiProvider;
    this.moshiProvider = moshiProvider;
    this.remarkDaoProvider = remarkDaoProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.contactReaderProvider = contactReaderProvider;
  }

  @Override
  public ContactRepository get() {
    return newInstance(apiProvider.get(), moshiProvider.get(), remarkDaoProvider.get(), deviceInfoProvider.get(), contactReaderProvider.get());
  }

  public static ContactRepository_Factory create(Provider<CrmApiService> apiProvider,
      Provider<Moshi> moshiProvider, Provider<RemarkDao> remarkDaoProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<ContactReader> contactReaderProvider) {
    return new ContactRepository_Factory(apiProvider, moshiProvider, remarkDaoProvider, deviceInfoProvider, contactReaderProvider);
  }

  public static ContactRepository newInstance(CrmApiService api, Moshi moshi, RemarkDao remarkDao,
      DeviceInfoProvider deviceInfo, ContactReader contactReader) {
    return new ContactRepository(api, moshi, remarkDao, deviceInfo, contactReader);
  }
}
