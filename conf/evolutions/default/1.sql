# Init schema

# --- !Ups

create table SPRINT (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  TITLE varchar(255) NOT NULL,
  NUM int(10) NOT NULL,
  PRIMARY KEY (id)
);

create table STORY (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (id)
);


# --- !Downs

drop table STORY;

drop table SPRINT;