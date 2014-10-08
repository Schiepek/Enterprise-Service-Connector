# --- !Ups

create table settings (
  id                        bigint not null,
  server_url                 varchar(255),
  constraint pk_settings primary key (id))
;


create sequence settings_seq;

# --- !Downs

drop table if exists settings cascade;

drop sequence if exists settings_seq;