CREATE SEQUENCE global_id_seq START WITH 1000 INCREMENT BY 50;

-- 2. Cấu trúc bảng users: Đồng bộ ID, Thời gian và Audit
ALTER TABLE "users" 
  ADD CONSTRAINT "require_contact_info" CHECK ("email" IS NOT NULL OR "phone_number" IS NOT NULL),
  ALTER COLUMN "kyc_status" SET NOT NULL,
  ALTER COLUMN "created_at" TYPE timestamptz,
  ALTER COLUMN "created_at" SET NOT NULL,
  ALTER COLUMN "updated_at" TYPE timestamptz,
  ALTER COLUMN "updated_at" SET NOT NULL,
  -- Gắn ID vào Sequence theo chuẩn IDENTITY để Hibernate chạy Batch Insert
ALTER COLUMN "id" SET DEFAULT nextval('global_id_seq');
-- 3. Cấu trúc bảng accounts: Đồng bộ ID và Thời gian
ALTER TABLE "accounts"
  ALTER COLUMN "created_at" TYPE timestamptz,
  ALTER COLUMN "created_at" SET NOT NULL,
  ALTER COLUMN "updated_at" TYPE timestamptz,
  ALTER COLUMN "updated_at" SET NOT NULL,
ALTER COLUMN "id" SET DEFAULT nextval('global_id_seq');
-- 4. Cấu trúc bảng transactions: Đổi tên cột và đồng bộ ID
ALTER TABLE "transactions"
  RENAME COLUMN "reference_number" TO "public_id";

ALTER TABLE "transactions"
  ALTER COLUMN "created_at" TYPE timestamptz,
  ALTER COLUMN "created_at" SET NOT NULL,
  ALTER COLUMN "updated_at" TYPE timestamptz,
  ALTER COLUMN "updated_at" SET NOT NULL,
ALTER COLUMN "id" SET DEFAULT nextval('global_id_seq');
-- 5. Đánh Index 
CREATE INDEX "idx_users_public_id" ON "users" ("public_id");
CREATE INDEX "idx_accounts_public_id" ON "accounts" ("public_id");
CREATE INDEX "idx_transactions_public_id" ON "transactions" ("public_id");
CREATE INDEX "idx_accounts_user_id" ON "accounts" ("user_id");
CREATE INDEX "idx_transactions_from_acc" ON "transactions" ("from_account_id");
CREATE INDEX "idx_transactions_to_acc" ON "transactions" ("to_account_id");