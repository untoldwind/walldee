# --- !Ups

alter table DISPLAYITEM add column PROJECTID bigint(20);

# --- !Downs

alter table DISPLAYITEM drop column PROJECTID;
