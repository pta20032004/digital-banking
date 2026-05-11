package com.tony.demo.infra.elasticsearch;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(indexName = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long transactionId;

    @Field(type = FieldType.Long)
    private Long fromAccountId;

    @Field(type = FieldType.Long)
    private Long toAccountId;

    @Field(type = FieldType.Double)
    private BigDecimal amount;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String transactionType;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Date)
    private Instant createdAt;
}
