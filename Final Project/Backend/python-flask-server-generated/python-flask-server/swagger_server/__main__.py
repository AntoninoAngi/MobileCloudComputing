#!/usr/bin/env python3

import connexion

from swagger_server import encoder


def main():
    app = connexion.App(__name__, specification_dir='./swagger/')
    app.app.json_encoder = encoder.JSONEncoder
    app.add_api('swagger.yaml', arguments={'title': 'Mobile Cloud Computing Project Backend (group 01)'})
    return app


if __name__ == '__main__':
    app = main()
    app.run(port=8080)
