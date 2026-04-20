CREATE TYPE "user_status" AS ENUM (
  'PENDING',
  'VERIFIED',
  'REJECTED'
);

CREATE TYPE "account_status" AS ENUM (
  'ACTIVE',
  'BLOCKED',
  'CLOSED'
);

CREATE TYPE "transaction_status" AS ENUM (
  'PENDING',
  'SUCCESS',
  'FAILED'
);

CREATE TYPE "transaction_type" AS ENUM (
  'DEPOSIT',
  'WITHDRAW',
  'TRANSFER'
);

CREATE TABLE "users" (
  "id" BIGINT PRIMARY KEY, 
  "full_name" varchar(255) NOT NULL,
  "date_of_birth" DATE NOT NULL,
  "username" varchar(50) UNIQUE NOT NULL,
  "email" varchar(100) UNIQUE,
  "password" varchar(255) NOT NULL,
  "phone_number" varchar(20) UNIQUE,
  "identity_number" varchar(50) UNIQUE NOT NULL,
  "kyc_status" user_status DEFAULT 'PENDING',
  "created_at" timestamp DEFAULT (now()),
  "updated_at" timestamp DEFAULT (now())
);

CREATE TABLE "accounts" (
  "id" BIGINT PRIMARY KEY,
  "user_id" BIGINT NOT NULL,
  "account_number" varchar(20) UNIQUE NOT NULL,
  "balance" decimal(19,4) NOT NULL DEFAULT 0,
  "status" account_status DEFAULT 'ACTIVE',
  "version" int DEFAULT 0,
  "currency" varchar(10) DEFAULT 'VND',
  "created_at" timestamp DEFAULT (now()),
  "updated_at" timestamp DEFAULT (now())
);

CREATE TABLE "transactions" (
  "id" BIGINT PRIMARY KEY,
  "reference_number" varchar(50) UNIQUE NOT NULL,
  "from_account_id" BIGINT,
  "to_account_id" BIGINT,
  "amount" decimal(19,4) NOT NULL CHECK (amount > 0),
  "fee" decimal(19,4) NOT NULL DEFAULT 0,
  "status" transaction_status DEFAULT 'PENDING',
  "transaction_type" transaction_type NOT NULL,
  "description" varchar(255),
  "from_post_balance" decimal(19,4),
  "to_post_balance" decimal(19,4),
  "created_at" timestamp DEFAULT (now()),
  "updated_at" timestamp DEFAULT (now())
);

-- Tạo khóa ngoại
ALTER TABLE "accounts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");
ALTER TABLE "transactions" ADD FOREIGN KEY ("from_account_id") REFERENCES "accounts" ("id");
ALTER TABLE "transactions" ADD FOREIGN KEY ("to_account_id") REFERENCES "accounts" ("id");

-- Đánh Index 
-- CREATE INDEX "idx_users_username" ON "users" ("username");
-- CREATE INDEX "idx_accounts_user_id" ON "accounts" ("user_id");
-- CREATE INDEX "idx_accounts_account_number" ON "accounts" ("account_number");
-- CREATE INDEX "idx_transactions_from_acc" ON "transactions" ("from_account_id");
-- CREATE INDEX "idx_transactions_to_acc" ON "transactions" ("to_account_id");