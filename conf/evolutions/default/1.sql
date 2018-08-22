# --- !Ups

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

drop table if exists tokens cascade;
drop sequence if exists tokens_id_seq;

