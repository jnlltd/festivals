alter table user change preferred_username social_id varchar(191);

update user set social_login_provider = lower(social_login_provider);
update user set social_login_provider = 'yahoo' where social_login_provider = 'yahoo!';
