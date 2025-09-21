"""
Product DAO (Data Access Object)
SPDX - License - Identifier: LGPL - 3.0 - or -later
Auteurs : Gabriel C. Ullmann, Fabio Petrillo, 2025
"""
import os
from dotenv import load_dotenv
import mysql.connector
from models.user import User

class ProductDAO:
    def __init__(self):
        try:
            env_path = "../.env"
            print(os.path.abspath(env_path))
            load_dotenv(dotenv_path=env_path)
            db_host = os.getenv("MYSQL_HOST")
            db_name = os.getenv("MYSQL_DB_NAME")
            db_user = os.getenv("DB_USERNAME")
            db_pass = os.getenv("DB_PASSWORD")     
            self.conn = mysql.connector.connect(host=db_host, user=db_user, password=db_pass, database=db_name)   
            self.cursor = self.conn.cursor()
        except FileNotFoundError as e:
            print("Attention : Veuillez créer un fichier .env")
        except Exception as e:
            print("Erreur : " + str(e))

    def select_all(self):
        """ Select all products from MySQL """
        pass

    def insert(self, product):
        """ Insert given product into MySQL """
        pass

    def update(self, product):
        """ Update given product in MySQL """
        pass

    def delete(self, product_id):
        """ Delete product from MySQL with given product ID """
        pass

    def delete_all(self): #optional
        """ Empty products table in MySQL """
        pass
        
    def close(self):
        self.cursor.close()
        self.conn.close()
