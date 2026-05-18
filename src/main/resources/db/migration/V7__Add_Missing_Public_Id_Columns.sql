-- Add missing public_id columns to tables whose Entities inherit from BaseEntity

ALTER TABLE "user_kyc_documents" ADD COLUMN "public_id" UUID UNIQUE DEFAULT gen_random_uuid();
ALTER TABLE "system_configs" ADD COLUMN "public_id" UUID UNIQUE DEFAULT gen_random_uuid();
ALTER TABLE "login_histories" ADD COLUMN "public_id" UUID UNIQUE DEFAULT gen_random_uuid();
ALTER TABLE "user_promotions" ADD COLUMN "public_id" UUID UNIQUE DEFAULT gen_random_uuid();
