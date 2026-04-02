#!/bin/bash

# ========================================
# RSA Key Pair Generator for JWT RS256
# Generate 2048-bit RSA keys for secure JWT signing
# ========================================

echo "🔐 =========================================="
echo "   RSA Key Pair Generator for JWT RS256"
echo "=========================================="
echo ""

# Create keys directory if it doesn't exist
if [ ! -d "keys" ]; then
    mkdir -p keys
    echo "✅ Created 'keys' directory"
else
    echo "📁 'keys' directory already exists"
fi

# Check if keys already exist
if [ -f "keys/private_key.pem" ] || [ -f "keys/public_key.pem" ]; then
    echo ""
    echo "⚠️  WARNING: Key files already exist!"
    echo "   - keys/private_key.pem"
    echo "   - keys/public_key.pem"
    echo ""
    read -p "Do you want to overwrite them? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        echo "❌ Operation cancelled"
        exit 0
    fi

    echo "🔄 Backing up existing keys..."
    timestamp=$(date +%Y%m%d_%H%M%S)
    if [ -f "keys/private_key.pem" ]; then
        cp keys/private_key.pem "keys/private_key_backup_${timestamp}.pem"
        echo "   - Backed up: private_key_backup_${timestamp}.pem"
    fi
    if [ -f "keys/public_key.pem" ]; then
        cp keys/public_key.pem "keys/public_key_backup_${timestamp}.pem"
        echo "   - Backed up: public_key_backup_${timestamp}.pem"
    fi
fi

echo ""
echo "🔑 Generating RSA key pair (2048-bit)..."
echo ""

# Generate Private Key (2048 bits)
openssl genrsa -out keys/private_key.pem 2048 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Private key generated: keys/private_key.pem"
else
    echo "❌ Failed to generate private key"
    exit 1
fi

# Extract Public Key from Private Key
openssl rsa -in keys/private_key.pem -pubout -out keys/public_key.pem 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Public key extracted: keys/public_key.pem"
else
    echo "❌ Failed to extract public key"
    exit 1
fi

# Set proper permissions
# Private key: Only owner can read (600)
# Public key: Everyone can read (644)
chmod 600 keys/private_key.pem
chmod 644 keys/public_key.pem
echo "🔒 File permissions set correctly"

echo ""
echo "=========================================="
echo "📊 Key Information:"
echo "=========================================="
openssl rsa -in keys/private_key.pem -text -noout 2>/dev/null | head -n 3

echo ""
echo "=========================================="
echo "✅ Success! RSA Key Pair Generated"
echo "=========================================="
echo ""
echo "📁 Files created:"
echo "   📄 keys/private_key.pem  (KEEP SECRET! 🔐)"
echo "   📄 keys/public_key.pem   (Can be shared 🌐)"
echo ""
echo "🔐 File Permissions:"
ls -lh keys/*.pem | grep -v backup
echo ""
echo "⚠️  IMPORTANT SECURITY NOTES:"
echo "   1. ❌ NEVER commit private_key.pem to Git"
echo "   2. ✅ Add 'keys/' to your .gitignore"
echo "   3. 🔒 Store private key securely in production:"
echo "      - AWS Secrets Manager"
echo "      - Azure Key Vault"
echo "      - HashiCorp Vault"
echo "      - Environment variables (Base64 encoded)"
echo "   4. 🔄 Rotate keys every 6-12 months"
echo "   5. 🚫 Never send private key over insecure channels"
echo ""
echo "📝 Next Steps:"
echo "   1. Update .gitignore:"
echo "      echo 'keys/' >> .gitignore"
echo ""
echo "   2. Update application.properties:"
echo "      jwt.private-key-path=classpath:keys/private_key.pem"
echo "      jwt.public-key-path=classpath:keys/public_key.pem"
echo ""
echo "   3. Copy keys to src/main/resources/keys/"
echo "      mkdir -p src/main/resources/keys"
echo "      cp keys/*.pem src/main/resources/keys/"
echo ""
echo "=========================================="
echo "🎉 Done!"
echo "=========================================="