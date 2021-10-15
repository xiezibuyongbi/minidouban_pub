USE MiniDouban;
CREATE TABLE UserInfo (
  user_id INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) UNIQUE,
  password VARCHAR(128) NOT NULL,
  email VARCHAR(48) UNIQUE
);