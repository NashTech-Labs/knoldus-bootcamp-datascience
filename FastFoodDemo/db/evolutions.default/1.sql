# --- First database schema

# --- !Ups

set ignorecase true;

create table company (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_company primary key (id))
;

create table users (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_users primary key (id))
;
