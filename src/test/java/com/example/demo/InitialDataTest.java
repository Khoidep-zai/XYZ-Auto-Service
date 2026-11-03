package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class InitialDataTest {
    @Test
    void demoAccountsAreCreatedOnlyWhenEnabled() throws Exception {
        var services = mock(ServiceRepository.class);
        var settings = mock(SettingsRepository.class);
        var accounts = mock(AccountRepository.class);
        var passwords = mock(PasswordEncoder.class);
        when(settings.existsById(1L)).thenReturn(true);
        when(services.count()).thenReturn(1L);
        when(passwords.encode(any())).thenReturn("hashed");

        var initialData = new InitialData();
        initialData.initializeWorkshop(services, settings, accounts, passwords, "", "", false).run(null);
        verify(accounts, never()).save(any());

        initialData.initializeWorkshop(services, settings, accounts, passwords, "", "", true).run(null);
        verify(accounts, times(3)).save(any());
    }
}
