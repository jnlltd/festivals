update festival set source = 'HUMAN';

update festival
set source = 'SKIDDLE'
where created_by_id = (select id from user where username = 'skiddle@mailinator.com');

alter table festival modify source varchar(255) not null;
alter table festival modify created_by_id bigint(20) null;
