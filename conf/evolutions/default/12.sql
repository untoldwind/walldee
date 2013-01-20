# --- !Ups

alter table DISPLAYITEM add column APPEARSINFEED boolean NOT NULL DEFAULT false;

# --- !Downs

alter table DISPLAYITEM drop column APPEARSINFEED;
