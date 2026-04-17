package org.maldeclabs.spider.application.services;

import jakarta.persistence.EntityNotFoundException;
import org.maldeclabs.spider.application.services.exceptions.*;
import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repository;

    private AccountRole getAccountRoleByOrdinal(int ordinal) {
        if(ordinal < 0 || ordinal >= AccountRole.values().length)
            throw new RoleNotFoundException("Role not found. Enter a valid role");
        return AccountRole.values()[ordinal];
    }


        public List<Account> findAll() {
        List<Account> users = repository.findAll();
        if (users.isEmpty()){
            throw new ResourceNotFoundException("There are no registered users");
        }
        return users;
    }

    public Account findById(String id) {
        Optional<Account> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Account findByEmail(String email){
        return repository.findByEmail(email);
    }

    public boolean existsByProfile(String profileName) {
        return repository.existsByProfile(profileName);
    }

    public boolean existsByEmail(String email){
        return repository.existsByEmail(email);
    }

    public Account findByEFPToken(String token){
        return repository.findByEFPToken(token);
    }

    public Account insert(Account obj) {
        repository.save(obj);
        return obj;
    }


    public void delete(String id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public Account update(String id, Account obj) {
        try {
            Account entity = repository.getReferenceById(id);
            updateData(entity, obj);
            return repository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    private void updateData(Account entity, Account obj) {
        entity.setName(obj.getName());
        entity.setProfile(obj.getProfile());
        entity.setEmail(obj.getEmail());
    }

    public Account updateRole(String id, int role){
        Account account = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found"));

        AccountRole newRole = getAccountRoleByOrdinal(role);

        if (account.getRole() == newRole)
            throw new AccountStatusException(String.format("The account status was already set to %s", newRole));

        account.setRole(newRole);
        return repository.save(account);
    }

    public Account updatePassword(String id, String oldPassword, String newPassword){
        Account account = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );

        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);

        if (bcrypt.matches(oldPassword, account.getPassword())) {
            if (bcrypt.matches(newPassword, encryptedPassword)){
                account.setPassword(encryptedPassword);
                return repository.save(account);
            }
            else{
                throw new PasswordUpdateException("The password must be encrypted to save in the database");
            }
        } else {
            throw new PasswordUpdateException("incorrect password");
        }
    }
}
