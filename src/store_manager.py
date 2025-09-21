"""
Store manager application
SPDX - License - Identifier: LGPL - 3.0 - or -later
Auteurs : Gabriel C. Ullmann, Fabio Petrillo, 2025
"""
from views.user_view import UserView

if __name__ == '__main__':
    print("===== LE MAGASIN DU COIN =====")
    main_menu = UserView()
    main_menu.show_options()
