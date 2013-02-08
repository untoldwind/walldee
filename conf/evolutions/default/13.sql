# --- !Ups

alter table DISPLAY add column RELATIVELAYOUT boolean NOT NULL DEFAULT false;

# --- !Downs

alter table DISPLAY drop column RELATIVELAYOUT;
