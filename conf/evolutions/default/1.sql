# --- First Database Schema

# -- !Ups

-- table declarations :
create table "serverstate" (
    "id" bigint DEFAULT nextval('s_state_id') primary key not null,
    "lastPoem" bigint not null
  );
create sequence "s_state_id";

# -- !Downs

DROP TABLE IF EXISTS "serverstate";
DROP SEQUENCE IF EXISTS "s_state_id";

