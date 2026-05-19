-- 1. Tạo bảng định nghĩa mẫu thông báo (Hỗ trợ đa ngôn ngữ)
CREATE TABLE "notification_templates" (
  "code" VARCHAR(50) PRIMARY KEY, -- VD: 'TXN_DEPOSIT', 'SYS_MAINTENANCE'
  "title_en" VARCHAR(255) NOT NULL,
  "title_vi" VARCHAR(255) NOT NULL,
  "content_en" TEXT NOT NULL, -- VD: "Your account was credited {amount} VND"
  "content_vi" TEXT NOT NULL, -- VD: "Tài khoản của bạn được cộng {amount} VND"
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. Dọn dẹp dữ liệu cũ (Xóa các thông báo broadcast cũ trước khi ép kiểu NOT NULL)
DELETE FROM "notifications" WHERE "user_id" IS NULL;

-- Xóa index thừa
DROP INDEX IF EXISTS "idx_notifications_broadcast_created";

-- 3. Cấu trúc lại bảng notifications hiện tại thành In-App thuần túy
ALTER TABLE "notifications" 
  ADD COLUMN "template_code" VARCHAR(50),
  ADD COLUMN "params" JSONB, -- Lưu dạng: {"amount": "500,000", "balance": "1,200,000"}
  ALTER COLUMN "title" DROP NOT NULL,
  ALTER COLUMN "content" DROP NOT NULL,
  ALTER COLUMN "user_id" SET NOT NULL;

-- Thêm khóa ngoại cho bảng notifications
ALTER TABLE "notifications" 
  ADD CONSTRAINT "fk_notification_template" 
  FOREIGN KEY ("template_code") REFERENCES "notification_templates" ("code");

-- 4. Tạo bảng thông báo chung (Broadcast)
CREATE TABLE "broadcast_notifications" (
  "id" BIGINT PRIMARY KEY DEFAULT nextval('global_id_seq'),
  "public_id" UUID UNIQUE NOT NULL,
  "title" VARCHAR(255),
  "content" TEXT,
  "template_code" VARCHAR(50), -- Hỗ trợ cả template cho Broadcast
  "params" JSONB,
  "type" notification_type NOT NULL DEFAULT 'SYSTEM',
  "action_url" VARCHAR(255),
  "start_time" TIMESTAMPTZ NOT NULL DEFAULT now(), -- Thời điểm bắt đầu hiển thị
  "end_time" TIMESTAMPTZ, -- Hết hạn ẩn đi
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT "fk_broadcast_template" FOREIGN KEY ("template_code") REFERENCES "notification_templates" ("code")
);

-- Index phục vụ tra cứu thông báo Broadcast
CREATE INDEX "idx_broadcast_notifications_time" ON "broadcast_notifications" ("start_time", "end_time");

-- 5. Tạo bảng trung gian để đánh dấu User đã đọc thông báo Broadcast chưa
CREATE TABLE "user_read_broadcasts" (
  "user_id" BIGINT NOT NULL,
  "broadcast_id" BIGINT NOT NULL,
  "read_at" TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY ("user_id", "broadcast_id"),
  CONSTRAINT "fk_read_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
  CONSTRAINT "fk_read_broadcast" FOREIGN KEY ("broadcast_id") REFERENCES "broadcast_notifications" ("id") ON DELETE CASCADE
);

-- 6. Bổ sung các Permission mới cho tính năng quản lý thông báo
INSERT INTO "permissions" (id, name, code) VALUES 
(2010, 'Gửi thông báo toàn hệ thống (Broadcast)', 'NOTI_SEND_BROADCAST'),
(2011, 'Gửi thông báo cá nhân (Direct)', 'NOTI_SEND_DIRECT'),
(2012, 'Quản lý mẫu thông báo', 'NOTI_TEMPLATE_MANAGE');

-- Phân quyền cho ADMIN (Có tất cả quyền)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1001, 2010),
(1001, 2011),
(1001, 2012);

-- Phân quyền cho MARKETING (Gửi broadcast, tạo/sửa template)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1251, 2010),
(1251, 2012);

-- Phân quyền cho SUPPORT (Gửi thông báo cá nhân cho user)
INSERT INTO "role_permissions" (role_id, permission_id) VALUES 
(1151, 2011);

-- 7. Đồng bộ lại Sequence để tránh Duplicate Key do insert cứng ID
SELECT setval('global_id_seq', (
  SELECT MAX(id) FROM (
    SELECT id FROM "roles" 
    UNION SELECT id FROM "permissions" 
    UNION SELECT id FROM "users"
    UNION SELECT id FROM "accounts"
    UNION SELECT id FROM "transactions"
    UNION SELECT id FROM "broadcast_notifications"
  ) AS combined_ids
));
