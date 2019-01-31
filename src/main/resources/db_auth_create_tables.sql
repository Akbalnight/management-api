DROP table IF EXISTS users cascade;
DROP table IF EXISTS roles cascade;
DROP table IF EXISTS user_roles cascade;
DROP table IF EXISTS permissions cascade;
DROP table IF EXISTS role_permissions cascade;
DROP table IF EXISTS ldap_roles cascade;

CREATE TABLE IF NOT EXISTS users(
   user_id SERIAL NOT NULL,
   username varchar(320) NOT NULL,
   password varchar(128) NOT NULL,
   enabled boolean NOT NULL DEFAULT FALSE,
   ldap boolean NOT NULL DEFAULT FALSE,
   email varchar(320),
   json_data json,
   primary key(username)
);

CREATE TABLE IF NOT EXISTS roles
(
  name varchar(64) PRIMARY KEY,
  description varchar(256),
  json_data json
);

create table IF NOT EXISTS user_roles (
  user_role_id SERIAL PRIMARY KEY,
  username varchar(320) NOT NULL,
  role varchar(64) NOT NULL REFERENCES roles (name) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE (username,role),
  FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS permissions
(
  id serial PRIMARY KEY,
  description varchar(256),
  method varchar(10),
  path varchar(1024),
  json_data json,
  UNIQUE (method,path)
);

CREATE TABLE IF NOT EXISTS role_permissions
(
  role varchar(64) REFERENCES roles (name) ON DELETE CASCADE ON UPDATE CASCADE,
  id_permission integer REFERENCES permissions (id) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE (role,id_permission)
);

CREATE TABLE IF NOT EXISTS ldap_roles
(
   ldap_group varchar(256),
   role varchar(64),
   FOREIGN KEY (role) REFERENCES roles (name) ON DELETE CASCADE ON UPDATE CASCADE,
   UNIQUE (ldap_group, role)
);

