# Create secrets directory and files
echo "Setting up secret files..."

mkdir -p ./secrets

# Create JWT secret file
echo "your-super-secret-jwt-key-here-32-chars!" > ./secrets/jwt_secret.txt

# Create encryption key file (must be 32 characters)
echo "your-32-char-encryption-key-here!!" > ./secrets/encryption_key.txt

# Create Groq API key file
echo "your-groq-api-key-here" > ./secrets/groq_api_key.txt

# Set proper permissions
chmod 600 ./secrets/*
chmod 700 ./secrets

echo "Secret files created successfully!"
echo "Files created:"
ls -la ./secrets/

echo ""
echo "Make sure to:"
echo "1. Add ./secrets/ to your .gitignore"
echo "2. Replace the dummy values with real secrets"
echo "3. Set proper SSL_KEYSTORE_PASSWORD in .env file"