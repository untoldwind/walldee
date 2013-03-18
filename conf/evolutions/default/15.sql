# --- !Ups

create table TEAM (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  NAME varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

alter table SPRINT add column TEAMID bigint(20);

# --- !Downs

alter table SPRINT drop column TEAMID;
drop table TEAM;