package com.friends.friends.Repository;

import com.friends.friends.Entity.Account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByEmail(String email);

    Optional<Account> findByIco(String ico);
    
    boolean existsByEmail(String email);
    
    Optional<Account> findByEmailAndIsBusiness(String email, Boolean isBusiness);

    boolean existsByIco(String ico);
}
