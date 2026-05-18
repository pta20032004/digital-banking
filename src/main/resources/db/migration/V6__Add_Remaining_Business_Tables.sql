-- 0. THÊM TRANSACTION PIN VÀO BẢNG USERS
ALTER TABLE "users" ADD COLUMN "transaction_pin" VARCHAR(255);


-- 1. BẢNG CHI TIẾT ĐỊNH DANH (user_kyc_documents)
CREATE TABLE "user_kyc_documents" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT UNIQUE NOT NULL, -- UNIQUE để đảm bảo quan hệ 1-1
  "id_card_front_url" VARCHAR(500) NOT NULL,
  "id_card_back_url" VARCHAR(500) NOT NULL,
  "selfie_url" VARCHAR(500) NOT NULL,
  "reviewed_by" BIGINT, -- staff_id người duyệt
  "rejection_reason" TEXT,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_kyc_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "fk_kyc_staff" FOREIGN KEY ("reviewed_by") REFERENCES "staffs" ("id")
);


-- 2. BẢNG CẤU HÌNH HỆ THỐNG (system_configs)
CREATE TABLE "system_configs" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "config_key" VARCHAR(100) UNIQUE NOT NULL,
  "config_value" TEXT NOT NULL,
  "description" VARCHAR(255),
  "updated_by" BIGINT, -- staff_id người cập nhật cuối
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_config_staff" FOREIGN KEY ("updated_by") REFERENCES "staffs" ("id")
);

-- 3. BẢNG LỊCH SỬ THAO TÁC (audit_logs)
CREATE TABLE "audit_logs" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL,
  "staff_id" BIGINT NOT NULL,
  "action" VARCHAR(100) NOT NULL, -- VD: UPDATE_CONFIG, APPROVE_KYC
  "target_entity" VARCHAR(100) NOT NULL, -- Bảng bị tác động (VD: system_configs, users)
  "target_id" VARCHAR(255) NOT NULL, -- ID của bản ghi bị tác động
  "old_data" JSONB, -- Dùng JSONB của Postgres để lưu lịch sử thay đổi
  "new_data" JSONB,
  "ip_address" VARCHAR(45),
  "user_agent" TEXT,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_audit_staff" FOREIGN KEY ("staff_id") REFERENCES "staffs" ("id")
);
CREATE INDEX "idx_audit_logs_staff" ON "audit_logs" ("staff_id");
CREATE INDEX "idx_audit_logs_entity" ON "audit_logs" ("target_entity", "target_id");

-- 4. BẢNG DANH BẠ THỤ HƯỞNG (beneficiaries)
CREATE TABLE "beneficiaries" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL,
  "user_id" BIGINT NOT NULL,
  "account_number" VARCHAR(50) NOT NULL,
  "bank_code" VARCHAR(50) NOT NULL DEFAULT 'INTERNAL', -- Phân biệt nội bộ hoặc ngân hàng khác
  "remind_name" VARCHAR(255) NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_beneficiary_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "unique_user_beneficiary" UNIQUE ("user_id", "account_number", "bank_code") -- Tránh lưu trùng 1 STK
);
CREATE INDEX "idx_beneficiaries_user" ON "beneficiaries" ("user_id");

-- 5. BẢNG LỊCH SỬ ĐĂNG NHẬP (login_histories)
CREATE TYPE "login_status" AS ENUM ('SUCCESS', 'FAILED');

CREATE TABLE "login_histories" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT, -- Có giá trị nếu là KH đăng nhập
  "staff_id" BIGINT, -- Có giá trị nếu là Nhân viên đăng nhập
  "ip_address" VARCHAR(45),
  "user_agent" TEXT,
  "status" login_status NOT NULL,
  "failure_reason" VARCHAR(255),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "chk_login_entity" CHECK (
    ("user_id" IS NOT NULL AND "staff_id" IS NULL) OR 
    ("staff_id" IS NOT NULL AND "user_id" IS NULL)
  ),
  CONSTRAINT "fk_login_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "fk_login_staff" FOREIGN KEY ("staff_id") REFERENCES "staffs" ("id") ON DELETE CASCADE
);
CREATE INDEX "idx_login_histories_user" ON "login_histories" ("user_id");
CREATE INDEX "idx_login_histories_staff" ON "login_histories" ("staff_id");

-- 6. BẢNG KHUYẾN MÃI (promotions & user_promotions)
CREATE TABLE "promotions" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL,
  "code" VARCHAR(50) UNIQUE NOT NULL,
  "name" VARCHAR(255) NOT NULL,
  "description" TEXT,
  "reward_amount" DECIMAL(19,4) NOT NULL CHECK (reward_amount > 0),
  "total_quantity" INT NOT NULL CHECK (total_quantity > 0),
  "used_quantity" INT NOT NULL DEFAULT 0,
  "start_time" TIMESTAMPTZ NOT NULL,
  "end_time" TIMESTAMPTZ NOT NULL,
  "is_active" BOOLEAN DEFAULT TRUE,
  "created_by" BIGINT NOT NULL, -- staff_id tạo mã
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_promo_staff" FOREIGN KEY ("created_by") REFERENCES "staffs" ("id")
);
CREATE INDEX "idx_promotions_code" ON "promotions" ("code");

CREATE TYPE "promo_status" AS ENUM ('CLAIMED', 'USED', 'EXPIRED');

CREATE TABLE "user_promotions" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "user_id" BIGINT NOT NULL,
  "promotion_id" BIGINT NOT NULL,
  "status" promo_status NOT NULL DEFAULT 'CLAIMED',
  "used_at" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_user_promo_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "fk_user_promo_promo" FOREIGN KEY ("promotion_id") REFERENCES "promotions" ("id") ON DELETE CASCADE,
  CONSTRAINT "unique_user_promo" UNIQUE ("user_id", "promotion_id") -- Mỗi user chỉ nhận 1 mã 1 lần
);
