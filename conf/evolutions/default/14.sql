# --- !Ups

alter table DISPLAY add column ANIMATIONCYCLES int(10) NOT NULL DEFAULT 0;
alter table DISPLAY add column ANIMATIONDELAY int(10) NOT NULL DEFAULT 0;
alter table DISPLAYITEM add column ANIMATIONCYCLES varchar(255);

# --- !Downs

alter table DISPLAYITEM drop column ANIMATIONCYCLES;
alter table DISPLAY drop column ANIMATIONDELAY;
alter table DISPLAY drop column ANIMATIONCYCLES;
