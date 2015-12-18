# User schema

# --- !Ups
create table `MachineLearnModel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR NOT NULL PRIMARY KEY,
  `sourceData` TEXT NOT NULL,
  `description` TEXT NOT NULL
)

# --- !Downs
drop table `MachineLearnModel`
