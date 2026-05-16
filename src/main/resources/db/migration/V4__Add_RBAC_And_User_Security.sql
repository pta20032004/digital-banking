-- V4__Add_RBAC_And_User_Security.sql

-- 1. Tạo bảng roles
CREATE TABLE "roles" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "name" VARCHAR(50) NOT NULL,
  "code" VARCHAR(20) UNIQUE NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. Tạo bảng permissions
CREATE TABLE "permissions" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "name" VARCHAR(100) NOT NULL,
  "code" VARCHAR(50) UNIQUE NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Tạo bảng role_permissions (Mối quan hệ N-N)
CREATE TABLE "role_permissions" (
  "role_id" BIGINT NOT NULL,
  "permission_id" BIGINT NOT NULL,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY ("role_id", "permission_id"),
  CONSTRAINT "fk_role" FOREIGN KEY ("role_id") REFERENCES "roles" ("id") ON DELETE CASCADE,
  CONSTRAINT "fk_permission" FOREIGN KEY ("permission_id") REFERENCES "permissions" ("id") ON DELETE CASCADE
);

-- 4. Cập nhật bảng users (Quan hệ 1-N: 1 User - 1 Role)
ALTER TABLE "users" 
  ADD COLUMN "is_enabled" BOOLEAN DEFAULT TRUE,
  ADD COLUMN "is_locked" BOOLEAN DEFAULT FALSE,
  ADD COLUMN "failed_attempt" INT DEFAULT 0,
  ADD COLUMN "lock_time" TIMESTAMPTZ,
  ADD COLUMN "role_id" BIGINT;

-- 5. Data Seeding cho Roles
INSERT INTO "roles" (id, name, code) VALUES 
(1001, 'Quản trị viên', 'ROLE_ADMIN'),
(1051, 'Quản lý vận hành', 'ROLE_MANAGER'),
(1101, 'Giao dịch viên', 'ROLE_TELLER'),
(1151, 'Hỗ trợ khách hàng', 'ROLE_SUPPORT'),
(1201, 'Kiểm toán viên', 'ROLE_AUDITOR'),
(1251, 'Nhân viên Marketing', 'ROLE_MARKETING'),
(1301, 'Khách hàng', 'ROLE_USER'),
(1351, 'Xác thực bước đầu', 'ROLE_PRE_AUTH');

-- 6. Gán Role mặc định (USER - 1301) cho các user hiện có & Ràng buộc NOT NULL
UPDATE "users" SET "role_id" = 1301 WHERE "role_id" IS NULL;

ALTER TABLE "users" 
  ALTER COLUMN "role_id" SET NOT NULL,
  ADD CONSTRAINT "fk_user_role" FOREIGN KEY ("role_id") REFERENCES "roles" ("id");

-- Đánh Index cho role_id ở bảng users
CREATE INDEX "idx_users_role_id" ON "users" ("role_id");

-- 7. Data Seeding cho Permissions
-- Sử dụng các ID từ 2001
INSERT INTO "permissions" (id, name, code) VALUES 
(2001, 'Duyệt hồ sơ định danh', 'KYC_APPROVE'),
(2002, 'Khóa tài khoản người dùng', 'USER_LOCK'),
(2003, 'Xem toàn bộ giao dịch hệ thống', 'TRANS_VIEW_ALL'),
(2004, 'Nạp/Rút tiền tại quầy cho khách', 'TRANS_HUB'),
(2005, 'Chỉnh thông số hệ thống (phí, hạn mức)', 'SYSTEM_CONFIG'),
(2006, 'Xem nhật ký thao tác của nhân viên', 'AUDIT_LOG_VIEW'),
(2007, 'Tạo mã giảm giá/vàng mã khuyến mãi', 'PROMO_CREATE'),
(2008, 'Tự thực hiện giao dịch (Chuyển tiền)', 'MY_TRANS_CREATE'),
(2009, 'Xem thông tin cơ bản người dùng', 'USER_READ_BASIC');

-- 8. Gán quyền cho từng Role (role_permissions)
-- ADMIN: Có tất cả quyền
INSERT INTO "role_permissions" (role_id, permission_id)
SELECT 1001, id FROM "permissions";

-- MANAGER: KYC_APPROVE (2001), USER_LOCK (2002), TRANS_VIEW_ALL (2003)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1051, 2001),
(1051, 2002),
(1051, 2003);

-- TELLER: TRANS_HUB (2004), USER_VIEW (2009)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1101, 2004),
(1101, 2009);

-- SUPPORT: USER_VIEW (2009), TRANS_VIEW_ALL (2003)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1151, 2009),
(1151, 2003);

-- AUDITOR: TRANS_VIEW_ALL (2003), AUDIT_LOG_VIEW (2006)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1201, 2003),
(1201, 2006);

-- MARKETING: PROMO_CREATE (2007), USER_VIEW (2009)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1251, 2007),
(1251, 2009);

-- USER: MY_TRANS_CREATE (2008)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1301, 2008);

-- 9. Đồng bộ lại Sequence để tránh Duplicate Key khi Hibernate gọi nextval()
-- Đã cẩn thận UNION thêm cả accounts và transactions (được tạo ở V1) vì dùng chung global_id_seq
SELECT setval('global_id_seq', (
  SELECT MAX(id) FROM (
    SELECT id FROM "roles" 
    UNION SELECT id FROM "permissions" 
    UNION SELECT id FROM "users"
    UNION SELECT id FROM "accounts"
    UNION SELECT id FROM "transactions"
  ) AS combined_ids
));

-- 10. Tạo loại thông báo để dễ phân loại ở lập trình backend/frontend
CREATE TYPE "notification_type" AS ENUM (
  'TRANSACTION', -- Biến động số dư (nạp, rút, chuyển tiền)
  'SECURITY',    -- Cảnh báo bảo mật (đổi mật khẩu, khóa tài khoản)
  'SYSTEM',      -- Thông báo hệ thống (bảo trì, cập nhật)
  'PROMOTION'    -- Khuyến mãi, mã giảm giá
);

-- 11. Tạo bảng thông báo
CREATE TABLE "notifications" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL, -- Dùng để truyền nhận qua API với Frontend (tránh lộ ID tăng dần)
  "user_id" BIGINT, -- Cho phép NULL để phục vụ thông báo Broadcast (toàn hệ thống)
  "title" VARCHAR(255) NOT NULL,
  "content" TEXT NOT NULL,
  "type" notification_type NOT NULL DEFAULT 'SYSTEM',
  "is_read" BOOLEAN NOT NULL DEFAULT FALSE,
  "read_at" TIMESTAMPTZ,
  "action_url" VARCHAR(255), -- Đường dẫn điều hướng khi user click vào thông báo trên web
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  
  CONSTRAINT "fk_notification_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE
);

-- 12. Đánh Index tối ưu hiệu năng
-- Index phục vụ tra cứu thông báo qua public_id từ API endpoint
CREATE INDEX "idx_notifications_public_id" ON "notifications" ("public_id");

-- Index phục vụ việc đếm số thông báo cá nhân CHƯA ĐỌC (tính năng hiển thị badge số 1, 2, 3... trên web)
CREATE INDEX "idx_notifications_user_unread" ON "notifications" ("user_id") WHERE "is_read" = FALSE AND "user_id" IS NOT NULL;

-- Index phục vụ việc lấy danh sách thông báo cá nhân mới nhất của user
CREATE INDEX "idx_notifications_user_created" ON "notifications" ("user_id", "created_at" DESC) WHERE "user_id" IS NOT NULL;

-- Index phục vụ việc quét các thông báo chung (Broadcast) toàn hệ thống mới nhất
CREATE INDEX "idx_notifications_broadcast_created" ON "notifications" ("created_at" DESC) WHERE "user_id" IS NULL;
