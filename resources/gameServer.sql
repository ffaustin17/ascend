
-- Drop the 'users' table if it already exists
DROP TABLE IF EXISTS users;

-- Create the 'users' table
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY, -- Username as the primary key
    password VARBINARY(255) NOT NULL, -- Stores the hashed password
    gamesPlayed INT DEFAULT 0, -- Tracks the number of games played
    gamesWon INT DEFAULT 0 -- Tracks the number of games won
);