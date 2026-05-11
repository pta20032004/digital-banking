package com.tony.demo.modules.transaction.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tony.demo.modules.transaction.domain.Transaction;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionRequest;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "transactionType", ignore = true)
    @Mapping(target = "fromPostBalance", ignore = true)
    @Mapping(target = "toPostBalance", ignore = true)
    @Mapping(target = "fee", ignore = true)
    @Mapping(target = "fromAccountId", ignore = true) // Handled in service
    @Mapping(target = "toAccountId", ignore = true) // Handled in service
    Transaction toEntity(TransactionRequest request);
}
