# --- !Ups

create table messages (
  id                            bigint not null,
  push_token_id                 bigint,
  transaction_id                varchar(255),
  attempts                      integer not null,
  locked_at                     timestamptz,
  sended                        boolean default false not null,
  last_error                    TEXT,
  created_at                    timestamptz not null,
  constraint pk_messages primary key (id)
);
create sequence messages_id_seq;

create table tokens (
  id                            bigint not null,
  token                         TEXT not null,
  provider                      varchar(255),
  address                       varchar(255),
  extended_push                 boolean default false not null,
  created_at                    timestamptz not null,
  constraint pk_tokens primary key (id)
);
create sequence tokens_id_seq;

create index ix_messages_push_token_id on messages (push_token_id);
alter table messages add constraint fk_messages_push_token_id foreign key (push_token_id) references tokens (id) on delete restrict on update restrict;


# --- !Downs

alter table if exists messages drop constraint if exists fk_messages_push_token_id;
drop index if exists ix_messages_push_token_id;

drop table if exists messages cascade;
drop sequence if exists messages_id_seq;

drop table if exists tokens cascade;
drop sequence if exists tokens_id_seq;

