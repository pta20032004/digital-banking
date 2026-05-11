package com.tony.demo.infra.elasticsearch;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSearchRepository extends ElasticsearchRepository<TransactionDocument, String> {
    List<TransactionDocument> findByDescriptionContaining(String keyword);
    List<TransactionDocument> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
}
