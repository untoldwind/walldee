# --- !Ups

create table TEAM (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  NAME varchar(255) NOT NULL,
  CURRENTSPRINTID bigint(20),
  PRIMARY KEY (id)
);

alter table SPRINT add column TEAMID bigint(20);
alter table DISPLAY add column TEAMID bigint(20);
alter table DISPLAYITEM add column TEAMID bigint(20);

# --- !Downs

alter table DISPLAYITEM drop column TEAMID;
alter table DISPLAY drop column TEAMID;
alter table SPRINT drop column TEAMID;
drop table TEAM;