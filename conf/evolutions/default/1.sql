# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table nodes (
  id                            integer not null,
  node_address                  varchar(255) not null,
  last_height                   bigint not null,
  created_at                    timestamptz not null,
  updated_at                    timestamptz not null,
  constraint pk_nodes primary key (id)
);
create sequence nodes_id_seq increment by 1;

create table tokens (
  id                            bigint not null,
  token                         TEXT not null,
  provider                      varchar(255),
  address                       varchar(255),
  extended_push                 boolean default false not null,
  created_at                    timestamptz not null,
  constraint pk_tokens primary key (id)
);
create sequence tokens_id_seq increment by 1;


# --- !Downs

drop table if exists nodes cascade;
drop sequence if exists nodes_id_seq;

drop table if exists tokens cascade;
drop sequence if exists tokens_id_seq;

