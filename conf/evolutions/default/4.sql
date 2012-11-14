# --- !Ups

alter table DISPLAY add column REFRESHTIME int(10) NOT NULL DEFAULT 5;
alter table SPRINT add column LANGUAGETAG varchar(255) NOT NULL DEFAULT 'de-DE';

# --- !Downs

alter table DISPLAY drop column REFRESHTIME;
alter table SPRINT drop column LANGUAGETAG;
