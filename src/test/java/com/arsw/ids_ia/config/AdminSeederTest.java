package com.arsw.ids_ia.config;

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.utils.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        adminSeeder = new AdminSeeder();
    }

    @Test
    void testSeedAdminsWithValidEmails() throws Exception {
        // Arrange
        String emails = "admin1@test.com,admin2@test.com";
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", emails);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository, times(2)).existsByEmail(anyString());
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testSeedAdminsWithExistingAdmin() throws Exception {
        // Arrange
        String emails = "existing@test.com,new@test.com";
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", emails);
        
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository, times(2)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSeedAdminsWithEmptyConfiguration() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", "");

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSeedAdminsWithNullConfiguration() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", null);

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSeedAdminsTrimsWhitespace() throws Exception {
        // Arrange
        String emails = "  admin1@test.com  ,  admin2@test.com  ";
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", emails);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository).existsByEmail("admin1@test.com");
        verify(userRepository).existsByEmail("admin2@test.com");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testSeedAdminsConvertsToLowercase() throws Exception {
        // Arrange
        String emails = "ADMIN@TEST.COM";
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", emails);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner = adminSeeder.seedAdmins(userRepository);

        // Act
        runner.run();

        // Assert
        verify(userRepository).existsByEmail("admin@test.com");
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    void testSeedAdminsSavesAdminWithCorrectRole() throws Exception {
        // Arrange
        String emails = "admin@test.com";
        ReflectionTestUtils.setField(adminSeeder, "initialAdmins", emails);

    }    
}       
