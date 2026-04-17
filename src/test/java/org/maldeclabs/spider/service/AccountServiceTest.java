package org.maldeclabs.spider.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.maldeclabs.spider.application.services.AccountService;
import org.maldeclabs.spider.application.services.exceptions.*;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository repository;


    // ####################################### Cases Testing with the findAll() #######################################

    @Test
    @DisplayName("findAll [case1] -> should return a list with two users")
    void findAllCase1() {

        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user1 = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);
        Account user2 = new Account("John Smith","jSmith",  "smith@example.com", "password", AccountRole.FREE, emailConfirmation);

        when(repository.findAll()).thenReturn(List.of(user1, user2));
        List<Account> result = accountService.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(user1, result.get(0));
        Assertions.assertEquals(user2, result.get(1));
    }

    @Test
    @DisplayName("findAll [case2] -> must throw an exception when don't have any registered users")
    void findAllCase2() {
        Mockito.when(repository.findAll()).thenReturn(Collections.emptyList());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> accountService.findAll());
    }

    // ################################################################################################################

    // ####################################### Cases Testing with the findById() ######################################

    @Test
    @DisplayName("findById [case1] -> must return the existing user by ID in the database")
    void findByIdCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);

        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        Account result = accountService.findById(user.getId());
        Assertions.assertEquals(result, user);
    }

    @Test
    @DisplayName("findById [case2] -> should throw an exception when the User Id does not exist")
    void findByIdCase2() {
        when(repository.findById("non_existent_id")).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> accountService.findById("non_existent_id"));
    }

    // ################################################################################################################

    // ######################################## Cases Testing with the insert() #######################################

    @Test
    @DisplayName("insert [case1] -> must successfully enter a user into the database")
    void insertCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                false,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);

        when(repository.save(user)).thenReturn(user);
        Account result = accountService.insert(user);

        Assertions.assertEquals(result, user);
    }

    @Test
    @DisplayName("insert [case2] -> should throw an exception when trying to save a user with incorrect information")
    void insertCase2() {
        Account user = new Account();
        when(repository.save(user)).thenThrow(DatabaseException.class);

        Assertions.assertThrows(DatabaseException.class, () -> accountService.insert(user));
    }

    // ################################################################################################################

    // #################################### Cases Testing with the delete() ###########################################

    @Test
    @DisplayName("delete [case1] -> must successfully delete a user from the database")
    void deleteCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);

        Mockito.doNothing().when(repository).deleteById(user.getId());
        accountService.delete(user.getId());

        Mockito.verify(repository).deleteById(user.getId());
    }

    @Test
    @DisplayName("delete [case2] -> must throw an exception when the user ID is non-existent")
    void deleteCase2() {
        Mockito.doThrow(ResourceNotFoundException.class).when(repository).deleteById("non_existent_id");

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.delete("non_existent_id")
        );
    }

    // ################################################################################################################

    // #################################### Cases Testing with the update() ###########################################

    @Test
    @DisplayName("update [case1] -> must successfully update a user's information from the database")
    void updateCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);

        when(repository.getReferenceById(user.getId())).thenReturn(user);

        user.setName("John D. Perts");
        user.setEmail("john445@mail.com");

        when(repository.save(user)).thenReturn(user);
        Account updatedUser = accountService.update(user.getId(), user);

        Assertions.assertEquals(updatedUser.getName(), "John D. Perts");
        Assertions.assertEquals(updatedUser.getEmail(), "john445@mail.com");
    }

    @Test
    @DisplayName("update [case2] -> must throw an exception when attempting to update a non-existent user's information")
    void updateCase2() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account("john Doe","johnD",  "john@example.com", "password", AccountRole.FREE, emailConfirmation);

        when(repository.getReferenceById("non_existent_id")).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.update("non_existent_id", user)
        );
    }

    // ################################################################################################################

    // ############################## Cases Testing with the updateRole() ###########################################

    @Test
    @DisplayName("updateRoleCase1 [case1] -> should throw an exception when trying to make an update from a user who already has that authority set")
    void updateRoleCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account(
                "john Doe",
                "johnD",
                "jonh@example.com",
                "password",
                AccountRole.BUSINESS,
                emailConfirmation
        );

        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        AccountStatusException exception = Assertions.assertThrows(AccountStatusException.class, () ->
                accountService.updateRole(user.getId(), 3)
        );

        Assertions.assertEquals("The account status was already set to BUSINESS", exception.getMessage());
    }


    @Test
    @DisplayName("updateRoleCase2 [case2] -> should update the role when the current role is different from the new role")
    void updateRoleCase2() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );
        Account user = new Account(
                "john Doe",
                "johnD",
                "jonh@example.com",
                "password",
                AccountRole.FREE,
                emailConfirmation
        );

        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        Account updatedAccount = accountService.updateRole(user.getId(), 2);

        Assertions.assertEquals(AccountRole.BASIC, user.getRole(), "The role should be updated to BASIC");
    }

    @Test
    @DisplayName("updateRoleCase3 [case2] -> should inform that the role is invalid if the role informed for update does not exist")
    void updateRoleCase3() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account(
                "john Doe",
                "johnD",
                "jonh@example.com",
                "password",
                AccountRole.FREE,
                emailConfirmation
        );

        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        // Verifica se a exceção é lançada ao passar uma role inválida
        RoleNotFoundException exception = Assertions.assertThrows(
                RoleNotFoundException.class,
                () -> accountService.updateRole(user.getId(), 4), // Role inválida
                "Expected RoleNotFoundException to be thrown"
        );

        Assertions.assertEquals("Role not found. Enter a valid role", exception.getMessage());
    }


    // ################################################################################################################

    // ########################## Cases Testing with the updatePassword() #############################################

    @Test
    @DisplayName("updatePassword [case1] -> Must update the user's password successfully")
    void updatePasswordCase1() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account(
                "john Doe",
                "johnD",
                "jonh@example.com",
                new BCryptPasswordEncoder().encode("password123"),
                AccountRole.FREE,
                emailConfirmation
        );

        String oldPassword = "password123";
        String newPassword = "NewPassword123";

        when(repository.save(user)).thenReturn(user);
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        Account updated = accountService.updatePassword(user.getId(), oldPassword, newPassword);

        Assertions.assertTrue(new BCryptPasswordEncoder().matches(newPassword, updated.getPassword()));
        Assertions.assertEquals(user.getPassword(), updated.getPassword());
    }

    @Test
    @DisplayName("updatePassword [case2] -> Must throw an ResourceNotFoundException when attempting to update the password of a non-existent user")
    void updatePasswordCase2() {
        String oldPassword = "password123";
        String newPassword = "NewPassword123";

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           this.accountService.updatePassword("non_existent_id", oldPassword, newPassword);
        });
    }

    @Test
    @DisplayName("updatePassword [case3] -> Should throw an PasswordUpdateException when the oldPassword is incorrect")
    void updatePasswordCase3() {
        EmailConfirmation emailConfirmation = new EmailConfirmation(
                true,
                String.valueOf(new Random().nextInt(9999 - 1000 + 1) + 1000),
                LocalDateTime.now().plusMinutes(1)
        );

        Account user = new Account(
                "john Doe",
                "johnD",
                "jonh@example.com",
                "password",
                AccountRole.FREE,
                emailConfirmation
        );

        String oldPassword = "password";
        String newPassword = "NewPassword123";

        when(repository.findById(user.getId())).thenReturn(Optional.of(user));

        Assertions.assertThrows(PasswordUpdateException.class, () -> {
            Account updated = accountService.updatePassword(user.getId(), oldPassword, newPassword);
        });
    }
    // ################################################################################################################

}