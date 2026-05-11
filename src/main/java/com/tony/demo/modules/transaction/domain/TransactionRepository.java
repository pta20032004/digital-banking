package com.tony.demo.modules.transaction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId, Pageable pageable);

}
