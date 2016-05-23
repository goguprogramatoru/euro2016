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

CREATE TABLE settings (
  key varchar PRIMARY KEY,
  val varchar
);

CREATE TABLE user_scores(
  user_name VARCHAR,
  game_key VARCHAR,
  team1_score INT,
  team2_score INT,
  PRIMARY KEY (user_name, game_key)
);

INSERT INTO users (user_name, password) VALUES ('admin','ACC8C9FD44646847FA77EC3C22EE7F50');    //SoccerPeanuts

INSERT INTO settings (key, val) VALUES ('winning_team_set_eta','2016-06-10 20:00:00')