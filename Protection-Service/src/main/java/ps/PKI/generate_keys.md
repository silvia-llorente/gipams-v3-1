$ openssl genrsa -out keypair.pem 2048
Generating RSA private key, 2048 bit long modulus
............+++
................................+++
e is 65537 (0x10001)
$ openssl rsa -in keypair.pem -outform DER -pubout -out public_key.der
writing RSA key
$ openssl pkcs8 -topk8 -nocrypt -in keypair.pem -outform DER -out private_key.der

