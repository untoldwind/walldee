# --- !Ups

create table ALARM (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  NAME varchar(255) NOT NULL,
  NEXTDATE timestamp NOT NULL,
  REPEATDAYS int(10),
  PRIMARY KEY (id)
);

# --- !Downs

drop table ALARM;
