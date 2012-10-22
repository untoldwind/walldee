# --- !Ups

alter table DISPLAY add column BACKGROUNDCOLOR varchar(255);

# --- !Downs

alter table DISPLAY drop column BACKGROUNDCOLOR;
