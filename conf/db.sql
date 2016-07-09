CREATE KEYSPACE euro2016 WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE euro2016

CREATE TABLE users (
  user_name varchar PRIMARY KEY,
  password varchar,
  winning_team varchar
);

CREATE TABLE user_tokens (
  user_name varchar PRIMARY KEY,
  user_token varchar
);

CREATE TABLE games (
  game_key varchar PRIMARY KEY,
  game_step varchar,
  game_date varchar,
  team1 varchar,
  team2 varchar,
  team1_score int,
  team2_score int
);

CREATE TABLE user_scores(
  user_name VARCHAR,
  game_key VARCHAR,
  team1_score INT,
  team2_score INT,
  PRIMARY KEY (user_name, game_key)
);

INSERT INTO users (user_name, password) VALUES ('admin','ACC8C9FD44646847FA77EC3C22EE7F50');    //SoccerPeanuts

CREATE TABLE kv(
  key_data VARCHAR PRIMARY KEY,
  value_data VARCHAR
);

INSERT INTO kv (key_data, value_data) VALUES ('champion','');

#TO CHANGE THE PASSWORD:


#Change the authenticator option in the cassandra.yaml file to PasswordAuthenticator.
#By default, the authenticator option is set to AllowAllAuthenticator.
#authenticator: PasswordAuthenticator
#restart server
#connect to client with defaults: ./cqlsh -u cassandra -p cassandra
ALTER USER cassandra WITH PASSWORD 'test';