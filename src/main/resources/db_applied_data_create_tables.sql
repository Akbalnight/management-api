drop table if exists user_settings;

create table if not exists user_settings(
	user_id bigint not null,
	settings json);