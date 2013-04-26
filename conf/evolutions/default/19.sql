# --- !Ups

alter table DISPLAYITEM drop column STYLENUM;
alter table DISPLAY drop column BACKGROUNDCOLOR;
alter table DISPLAY add column STYLENUM int(10) NOT NULL DEFAULT 0;


# --- !Downs

alter table DISPLAY drop column STYLENUM;
alter table DISPLAY add column BACKGROUNDCOLOR varchar(255) default '#000000';
alter table DISPLAYITEM add column STYLENUM int(10) NOT NULL DEFAULT 0;
