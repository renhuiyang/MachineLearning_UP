# User schema

# --- !Ups
create table `MachineLearnModel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `sourceData` TEXT NOT NULL,
  `description` TEXT NOT NULL
)

# --- !Downs
drop table `MachineLearnModel`
