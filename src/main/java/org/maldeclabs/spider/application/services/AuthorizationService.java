package org.maldeclabs.spider.application.services;

import org.maldeclabs.spider.domain.entities.Account;
import org.maldeclabs.spider.domain.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    AccountRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account user = repository.findByEmail(email);
        if (user == null){
            throw new UsernameNotFoundException("Account not found with email: " + email);
        }
        return user;
    }
}
