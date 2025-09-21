-- Créer le tableau Users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(80) NOT NULL
);

-- Créer des enregistrements dans Users
INSERT INTO users (name, email) VALUES
('Ada Lovelace', 'alovelace@example.com'),
('Adele Goldberg', 'agoldberg@example.com'),
('Alan Turing', 'aturing@example.com');
