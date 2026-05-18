-- 1. Tạo bảng staffs
CREATE TABLE "staffs" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL,
  "employee_code" VARCHAR(50) UNIQUE NOT NULL,
  "full_name" VARCHAR(255) NOT NULL,
  "email" CITEXT UNIQUE NOT NULL,
  "password" VARCHAR(255) NOT NULL,
  "role_id" BIGINT NOT NULL,
  "department" VARCHAR(100),
  "branch_code" VARCHAR(50),
  "is_enabled" BOOLEAN DEFAULT TRUE,
  "is_locked" BOOLEAN DEFAULT FALSE,
  "failed_attempt" INT DEFAULT 0,
  "lock_time" TIMESTAMPTZ,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  
  CONSTRAINT "fk_staff_role" FOREIGN KEY ("role_id") REFERENCES "roles" ("id")
);

-- Đánh index cho bảng staffs
CREATE INDEX "idx_staffs_public_id" ON "staffs" ("public_id");
CREATE INDEX "idx_staffs_role_id" ON "staffs" ("role_id");
CREATE INDEX "idx_staffs_employee_code" ON "staffs" ("employee_code");

-- 2. Gỡ bỏ khóa ngoại và Index role_id ở bảng users (đã tạo ở V4)
ALTER TABLE "users" DROP CONSTRAINT IF EXISTS "fk_user_role";
DROP INDEX IF EXISTS "idx_users_role_id";

-- 3. Xóa các cột quản trị nội bộ khỏi bảng users
ALTER TABLE "users" 
  DROP COLUMN IF EXISTS "role_id",
  DROP COLUMN IF EXISTS "is_enabled",
  DROP COLUMN IF EXISTS "is_locked",
  DROP COLUMN IF EXISTS "failed_attempt",
  DROP COLUMN IF EXISTS "lock_time";
