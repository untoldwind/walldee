# --- !Ups

insert into TEAM(ID, NAME) values (-1, 'Default Team');
update SPRINT set TEAMID = -1 where TEAMID is null;
alter table SPRINT alter column TEAMID bigint(20) not null;

# --- !Downs

alter table SPRINT alter column TEAMID bigint(20) null;
update SPRINT set TEAMID = null where TEAMID = -1;
delete from TEAM where ID = -1;

