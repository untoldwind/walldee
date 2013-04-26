# --- !Ups

alter table DISPLAY DROP COLUMN SPRINTID;
update DISPLAY set TEAMID = -1 where TEAMID is null;

# --- !Downs

alter table DISPLAY ADD COLUMN SPRINTID bigint(20) NULL;
update DISPLAY set TEAMID = null where TEAMID = -1;

