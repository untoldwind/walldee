# --- !Ups

alter table DISPLAY add column USELONGPOLLING boolean NOT NULL DEFAULT false;

# --- !Downs

alter table DISPLAY drop column USELONGPOLLING;
