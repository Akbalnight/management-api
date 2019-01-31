CREATE TABLE IF NOT EXISTS audit
(
 "time" timestamp without time zone,
 sessionid character varying(128),
 "user" character varying(128),
 userid character varying(12),
 rq character varying(1),
 data json
);

DROP INDEX IF EXISTS audit_index;
CREATE INDEX audit_index ON audit(
    "time",
    userid,
	(data->>'code'),
	(data->'requestJson'->>'time'),
	(data->'requestJson'->>'method'),
	(data->'requestJson'->>'path'));