-- V2__Modify_Id_To_UUID.sql
-- Database is blank
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto"; uuidv7 is native
CREATE EXTENSION IF NOT EXISTS "citext";

-- 1. Sửa cột ID của bảng users sang UUID và validate mail
ALTER TABLE "users" 
  ADD COLUMN "public_id" UUID UNIQUE NOT NULL, -- để backend tự sinh uuidv7
  ALTER COLUMN "email" TYPE CITEXT,
  ADD CONSTRAINT "email_must_be_valid" CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- 2. Cập nhật các cột liên quan ở bảng accounts và transactions
ALTER TABLE "accounts" ADD COLUMN "public_id" UUID UNIQUE NOT NULL;
ALTER TABLE "transactions" ALTER COLUMN "reference_number" TYPE UUID USING "reference_number"::uuid;
  
  