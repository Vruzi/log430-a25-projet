"""
User model
SPDX - License - Identifier: LGPL - 3.0 - or -later
Auteurs : Gabriel C. Ullmann, Fabio Petrillo, 2025
"""

class User:
    def __init__(self, user_id, name, email):
        self.id = user_id
        self.name = name
        self.email = email