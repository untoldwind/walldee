# --- !Ups

alter table DISPLAY add column REFRESHTIME int(10) NOT NULL DEFAULT 5;

# --- !Downs

alter table DISPLAY drop column REFRESHTIME;
