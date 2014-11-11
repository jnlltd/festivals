-- use 'show create table competition' to see the constraint name
alter table competition drop foreign key FKBEB591BFE3F6F81A;
alter table competition drop column winner_id;
