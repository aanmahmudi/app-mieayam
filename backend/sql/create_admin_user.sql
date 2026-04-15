CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO app_user (username, password_hash, roles_csv)
VALUES (
	:'username',
	crypt(:'password', gen_salt('bf', 10)),
	'ROLE_ADMIN,ROLE_USER'
)
ON CONFLICT (username) DO UPDATE
SET roles_csv = EXCLUDED.roles_csv,
	password_hash = EXCLUDED.password_hash;

