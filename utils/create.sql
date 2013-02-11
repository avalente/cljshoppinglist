CREATE SCHEMA shoppinglist;

CREATE DOMAIN shoppinglist.list AS TEXT;
COMMENT ON DOMAIN shoppinglist.list IS 'A JSON-encoded list of objects with at least the text field "item"';

CREATE TABLE shoppinglist.lists(
    "user" TEXT PRIMARY KEY,
    data shoppinglist.list NOT NULL,
    created TIMESTAMP(5) WITH TIME ZONE DEFAULT current_timestamp,
    updated TIMESTAMP(5) WITH TIME ZONE);

CREATE OR REPLACE FUNCTION shoppinglist.update_timestamp()
	RETURNS TRIGGER AS $$
	BEGIN
	   NEW.updated = now(); 
	   RETURN NEW;
	END;
	$$ language 'plpgsql';

CREATE TRIGGER update_shoppinglists_timestamp BEFORE UPDATE
    ON shoppinglist.lists FOR EACH ROW EXECUTE PROCEDURE 
    shoppinglist.update_timestamp();

CREATE TABLE shoppinglist.users(
    email TEXT PRIMARY KEY,
    real_name TEXT NOT NULL,
    password TEXT NOT NULL,
    created TIMESTAMP(5) WITH TIME ZONE DEFAULT current_timestamp,
    roles TEXT[] NOT NULL DEFAULT ARRAY['user']::TEXT[],
    ip TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    request_id UUID NULL);

