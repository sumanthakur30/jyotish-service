-- Local / RDS: create Jyotish database (same pattern as crmdb).
-- Run as postgres superuser, e.g.:
--   "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -f create-jyotishdb.sql

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'jyotishdb') THEN
    CREATE ROLE jyotishdb LOGIN PASSWORD 'jyotishdb';
  END IF;
END
$$;

SELECT 'CREATE DATABASE jyotishdb OWNER jyotishdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'jyotishdb')\gexec

GRANT ALL PRIVILEGES ON DATABASE jyotishdb TO jyotishdb;

\c jyotishdb
GRANT USAGE, CREATE ON SCHEMA public TO jyotishdb;
ALTER SCHEMA public OWNER TO jyotishdb;
