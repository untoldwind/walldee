# --- !Ups

alter table DISPLAY add column ANIMATIONCONFIG varchar(1000) NOT NULL DEFAULT '{}';
alter table DISPLAYITEM add column HIDDEN boolean NOT NULL DEFAULT false;

# --- !Downs

alter table DISPLAYITEM drop column HIDDEN;
alter table DISPLAY drop column ANIMATIONCONFIG;
