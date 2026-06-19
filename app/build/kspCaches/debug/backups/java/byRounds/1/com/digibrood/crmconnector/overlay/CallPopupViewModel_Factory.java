package com.digibrood.crmconnector.overlay;

import com.digibrood.crmconnector.data.repository.ContactRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CallPopupViewModel_Factory implements Factory<CallPopupViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  public CallPopupViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
  }

  @Override
  public CallPopupViewModel get() {
    return newInstance(contactRepositoryProvider.get());
  }

  public static CallPopupViewModel_Factory create(
      Provider<ContactRepository> contactRepositoryProvider) {
    return new CallPopupViewModel_Factory(contactRepositoryProvider);
  }

  public static CallPopupViewModel newInstance(ContactRepository contactRepository) {
    return new CallPopupViewModel(contactRepository);
  }
}
