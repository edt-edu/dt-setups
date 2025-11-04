CREATE TABLE "storage_unit"(
    "id" BIGINT NOT NULL,
    "unit_name" TEXT NOT NULL,
    "relative_position" TEXT NOT NULL
);
ALTER TABLE
    "storage_unit" ADD PRIMARY KEY("id");
CREATE TABLE "container"(
    "id" BIGINT NOT NULL,
    "container_name" TEXT NOT NULL,
    "unit_id" BIGINT NOT NULL,
    "item_type" TEXT NULL,
    "item_qty" INTEGER NULL,
    "item_weight_kg" FLOAT(53) NULL,
    "item_date_in" DATE NULL,
    "status_updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL
);
ALTER TABLE
    "container" ADD PRIMARY KEY("id");
CREATE TABLE "arm_operation"(
    "arm_id" BIGINT NOT NULL,
    "container_id" BIGINT NOT NULL,
    "operation_type" TEXT NOT NULL,
    "operation_timestamp" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "trigger_source" TEXT NOT NULL
);
ALTER TABLE
    "arm_operation" ADD PRIMARY KEY("arm_id");
CREATE TABLE "transition"(
    "name" TEXT NOT NULL,
    "first" TEXT NOT NULL,
    "guard_condition" TEXT NULL,
    "then" TEXT NOT NULL,
    "parent_name" TEXT NOT NULL
);
ALTER TABLE
    "transition" ADD PRIMARY KEY("name");
CREATE TABLE "state"(
    "name" TEXT NOT NULL,
    "action_type" TEXT NULL,
    "action_content" TEXT NULL,
    "parent_name" TEXT NOT NULL
);
ALTER TABLE
    "state" ADD PRIMARY KEY("name");
CREATE TABLE "parent"(
    "name" TEXT NOT NULL,
    "type" TEXT NOT NULL
);
ALTER TABLE
    "parent" ADD PRIMARY KEY("name");
ALTER TABLE
    "arm_operation" ADD CONSTRAINT "arm_operation_container_id_foreign" FOREIGN KEY("container_id") REFERENCES "container"("id");
ALTER TABLE
    "state" ADD CONSTRAINT "state_parent_name_foreign" FOREIGN KEY("parent_name") REFERENCES "parent"("name");
ALTER TABLE
    "container" ADD CONSTRAINT "container_unit_id_foreign" FOREIGN KEY("unit_id") REFERENCES "storage_unit"("id");
ALTER TABLE
    "transition" ADD CONSTRAINT "transition_parent_name_foreign" FOREIGN KEY("parent_name") REFERENCES "parent"("name");