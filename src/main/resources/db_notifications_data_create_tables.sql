drop table if exists notifications_data; 
CREATE TABLE notifications_data( 
        id bigint not null unique,
        description varchar(256),
        email_title varchar(256) NOT NULL,
        email_body varchar(2048) NOT NULL,
        props json);