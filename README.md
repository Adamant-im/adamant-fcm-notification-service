# ADAMANT FCM notification service

To successfully start the project, you need a postgres database, and also create the following environment variables:

export ADAMANT_FCM_SECRET = 'Your application secret'

export ADAMANT_FCM_DATABASE_URL = 'jdbc: postgresql: //127.0.0.1: 5432 / Your database name? charSet = utf8'

export ADAMANT_FCM_DATABASE_LOGIN = 'Your database login'

export ADAMANT_FCM_DATABASE_PASSWORD = 'Your datbase password'

export ADAMANT_FCM_SERVER_KEY_FILE_PATH = 'Path to server admin json file (Take it in firebase console)'

export ADAMANT_FCM_ADAMANT_ADDRESS = 'Your adamant address for subscriptions'

export ADAMANT_FCM_ADAMANT_PRIVATE_KEY = 'Your adamant private key in hex string'

export GCLOUD_PROJECT = 'adamant-messenger'