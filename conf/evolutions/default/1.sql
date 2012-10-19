# Init schema

# --- !Ups

create table SPRINT (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  TITLE varchar(255) NOT NULL,
  NUM int(10) NOT NULL,
  PRIMARY KEY (id)
);

create table STORY (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  TAG varchar(50) NOT NULL,
  DESCRIPTION varchar(1000) NOT NULL,
  POINTS int(10) NOT NULL,
  SPRINTID bigint(20) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (SPRINTID) REFERENCES SPRINT(ID)
);


# --- !Downs

drop table STORY;

drop table SPRINT;