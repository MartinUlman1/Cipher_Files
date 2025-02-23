# Application for downloading calls records

The application downloads user data and call centre call recordings. It then encrypts these files and sends them
to an SFTP server.
The application will start according to the set time for example (every day at 12:00)

## Configuration

output.file-path = folder to which xml and mp3 files will be downloaded
sftp.path-privateKey = path to the private key for access to sftp
sftp.passPhrase = password to access sftp
encrypt.inputFile = path to the folder where the files we want to encrypt are located
encrypt.outputFile = path to which folder to save the encrypted data
encrypt.outputFileAesKey = folder where the encrypted AES key will be stored
encrypt.outputFileVector = folder where the encrypted IV vector will be stored
encrypt.inputFilePublicKey = path to the public key for encoding the vector and the AES key
sftp.remote-directory = path where to send the encrypted files on the sftp server
sftp.local-directory = path from which folder we want to take the data and send it to the remote storage (SFTP)

## Installation

1: Install java 11+
2: Copy the jar file and run via command line

## Guidelines

To run the program, use the command line. You go to the application folder where the jar file goes, for example
C:/JavaApp/Name_app.jar, and enter the command java -jar Name_app.jar. This will start the application to download
the data for the present day.  If you want to use the application in test mode, just add -t. java -jar Name_app.jar -t.

## Autor

Martin Ulman
ulmi.3@seznam.cz
