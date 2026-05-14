package com.tony.demo.modules.transaction.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
// how chặn việc sửa vào trans
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByPublicId(UUID publicId);


    // Sao kê, lấy toàn bộ lịch sử giao dịch, có phân trang
    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId 
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findTransactionHistory(@Param("accountId") Long accountId, Pageable pageable);


   
    // 3. NGHIỆP VỤ BACKGROUND WORKER / RETRY (ĐẶC SẢN CỦA POSTGRESQL)
    // Lấy các giao dịch đang PENDING để xử lý (ví dụ: gọi API ngân hàng khác bị timeout cần retry).
    // Dùng FOR UPDATE SKIP LOCKED: 
    // -> Worker 1 vào lấy 10 record đầu và khóa lại.
    // -> Worker 2 vào sẽ BỎ QUA 10 record đang bị khóa và lấy 10 record tiếp theo.
    // Cách này giúp chạy song song nhiều Worker mà không bị conflict hay Deadlock.
    @Query(value = """
        SELECT * FROM transactions 
        WHERE status = 'PENDING' 
        ORDER BY created_at ASC 
        LIMIT :limit 
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<Transaction> findPendingTransactionsForWorkerNative(@Param("limit") int limit);
}