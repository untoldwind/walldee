# --- !Ups

alter table ALARM add column DURATIONMINS int(10) NOT NULL DEFAULT 15;

# --- !Downs

alter table ALARM drop column DURATIONMINS;
