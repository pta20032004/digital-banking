-- 2. NHÓM BẢO MẬT & PHÂN QUYỀN (SECURITY) - 5 BẢNG

-- Bảng roles: Phân quyền (User, Admin, Teller)
CREATE TABLE "roles" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "name" VARCHAR(50) UNIQUE NOT NULL,
  "description" VARCHAR(255)
);

-- Bảng user_roles: Bảng trung gian n-n
CREATE TABLE "user_roles" (
  "user_id" BIGINT NOT NULL,
  "role_id" BIGINT NOT NULL,
  PRIMARY KEY ("user_id", "role_id"),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("role_id") REFERENCES "roles" ("id") ON DELETE CASCADE
);

-- Bảng refresh_tokens: Quản lý phiên đăng nhập
CREATE TABLE "refresh_tokens" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "token" VARCHAR(500) UNIQUE NOT NULL,
  "expiry_date" TIMESTAMPTZ NOT NULL,
  "is_revoked" BOOLEAN DEFAULT FALSE,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE
);

-- Bảng otp_records: Xác thực giao dịch 2 bước
CREATE TABLE "otp_records" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "transaction_id" BIGINT, 
  "otp_code" VARCHAR(255) NOT NULL, 
  "expires_at" TIMESTAMPTZ NOT NULL,
  "is_used" BOOLEAN DEFAULT FALSE,
  "attempt_count" INT DEFAULT 0,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("transaction_id") REFERENCES "transactions" ("id") ON DELETE CASCADE
);

-- Bảng cards: Quản lý thẻ ngân hàng (Thẻ ATM, Visa, Mastercard)
CREATE TABLE "cards" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "account_id" BIGINT NOT NULL,
  "card_number" VARCHAR(20) UNIQUE NOT NULL,
  "card_holder_name" VARCHAR(255) NOT NULL,
  "card_type" VARCHAR(20) NOT NULL, -- DEBIT, CREDIT
  "expiration_date" DATE NOT NULL,
  "cvv" VARCHAR(10) NOT NULL,
  "pin" VARCHAR(255) NOT NULL,
  "status" VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, EXPIRED
  "public_id" UUID UNIQUE NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE
);

-- Bảng kyc_documents: Lưu hồ sơ định danh
CREATE TABLE "kyc_documents" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "document_type" VARCHAR(50) NOT NULL, -- FRONT_CCCD, BACK_CCCD, SELFIE
  "document_url" VARCHAR(500) NOT NULL, -- S3 URL
  "verification_status" VARCHAR(20) DEFAULT 'PENDING',
  "uploaded_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "verified_at" TIMESTAMPTZ,
  "verified_by" BIGINT, -- ID của Teller/Admin duyệt
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("verified_by") REFERENCES "users" ("id") ON DELETE SET NULL
);


-- 3. NHÓM VẬN HÀNH & TRẢI NGHIỆM (OPERATIONS & UX) - 4 BẢNG

-- Bảng beneficiaries: Danh sách người thụ hưởng
CREATE TABLE "beneficiaries" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "beneficiary_account_number" VARCHAR(50) NOT NULL,
  "beneficiary_name" VARCHAR(255) NOT NULL,
  "bank_name" VARCHAR(100) NOT NULL,
  "nickname" VARCHAR(100),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "uq_user_beneficiary" UNIQUE ("user_id", "beneficiary_account_number", "bank_name")
);

-- Bảng notifications: Lưu lịch sử thông báo
CREATE TABLE "notifications" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "title" VARCHAR(255) NOT NULL,
  "content" TEXT NOT NULL,
  "type" VARCHAR(50), -- BALANCE_UPDATE, SECURITY_ALERT, PROMOTION
  "is_read" BOOLEAN DEFAULT FALSE,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE
);

-- Bảng audit_logs: Nhật ký hệ thống (Lưu vết chỉnh sửa)
CREATE TABLE "audit_logs" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT, -- Ai thực hiện (có thể null nếu system action)
  "action" VARCHAR(100) NOT NULL, -- CREATE_USER, UPDATE_BALANCE, CHANGE_CONFIG
  "entity_name" VARCHAR(100) NOT NULL, -- Tên bảng/đối tượng bị ảnh hưởng
  "entity_id" VARCHAR(100), -- ID của record bị ảnh hưởng
  "old_value" JSONB, -- Dữ liệu cũ (dùng JSONB cực tiện cho audit)
  "new_value" JSONB, -- Dữ liệu mới
  "ip_address" VARCHAR(50),
  "user_agent" VARCHAR(500),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE SET NULL
);

-- Bảng system_configs: Cấu hình hệ thống (Thay đổi không cần deploy)
CREATE TABLE "system_configs" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "config_key" VARCHAR(100) UNIQUE NOT NULL, -- Ví dụ: MIN_TRANSFER_AMOUNT, MAINTENANCE_MODE
  "config_value" VARCHAR(500) NOT NULL,
  "description" TEXT,
  "updated_by" BIGINT, -- Ai là người đổi cấu hình này gần nhất
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  FOREIGN KEY ("updated_by") REFERENCES "users" ("id") ON DELETE SET NULL
);

-- 4. TẠO INDEX ĐỂ TỐI ƯU HIỆU NĂNG TRUY VẤN
CREATE INDEX "idx_refresh_tokens_token" ON "refresh_tokens" ("token");
CREATE INDEX "idx_refresh_tokens_user_id" ON "refresh_tokens" ("user_id");
CREATE INDEX "idx_otp_records_user_id" ON "otp_records" ("user_id");
CREATE INDEX "idx_kyc_documents_user_id" ON "kyc_documents" ("user_id");
CREATE INDEX "idx_beneficiaries_user_id" ON "beneficiaries" ("user_id");
CREATE INDEX "idx_notifications_user_id" ON "notifications" ("user_id");
CREATE INDEX "idx_notifications_created_at" ON "notifications" ("created_at");
CREATE INDEX "idx_audit_logs_entity" ON "audit_logs" ("entity_name", "entity_id");
CREATE INDEX "idx_audit_logs_created_at" ON "audit_logs" ("created_at");
CREATE INDEX "idx_cards_account_id" ON "cards" ("account_id");
CREATE INDEX "idx_cards_public_id" ON "cards" ("public_id");
