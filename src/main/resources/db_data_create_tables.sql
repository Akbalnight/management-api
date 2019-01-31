DROP table IF EXISTS role_objects;

CREATE TABLE role_objects
(
  role varchar(64) NOT NULL,
  service varchar(64) NOT NULL,
  objects varchar
);