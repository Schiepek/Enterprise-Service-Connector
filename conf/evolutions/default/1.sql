# --- !Ups

create table apiconfig (
  id                        bigint not null,
  client_id                 varchar(255),
  client_secret             varchar(255),
  redirect_uri              varchar(255),
  access_token              varchar(255),
  refresh_token             varchar(255),
  instance                  varchar(255),
  mail                      varchar(255),
  provider                  integer,
  constraint ck_apiconfig_provider check (provider in (0,1,2,3)),
  constraint pk_apiconfig primary key (id))
;


create sequence apiconfig_seq;

# --- !Downs

drop table if exists apiconfig cascade;

drop sequence if exists apiconfig_seq;