#!/usr/bin/env node
/**
 * JWT Token Generator for CallCard Microservice
 *
 * Generates JWT tokens compatible with the CallCard microservice JWT configuration.
 * Tokens are signed using HS512 algorithm with the same secret as the microservice.
 *
 * Usage:
 *   node generate-jwt-tokens.js
 *   node generate-jwt-tokens.js --username "testuser" --roles "ROLE_USER,ROLE_ADMIN"
 *   node generate-jwt-tokens.js --help
 */

const crypto = require('crypto');

// Parse command line arguments
const args = process.argv.slice(2);
const getArg = (name, defaultValue) => {
    const index = args.indexOf(name);
    return index !== -1 && args[index + 1] ? args[index + 1] : defaultValue;
};

if (args.includes('--help') || args.includes('-h')) {
    console.log(`
JWT Token Generator for CallCard Microservice

Usage:
  node generate-jwt-tokens.js [options]

Options:
  --username <name>     Username for the token (default: testuser)
  --roles <roles>       Comma-separated roles (default: ROLE_USER)
  --expiry <hours>      Token expiry in hours (default: 24)
  --secret <secret>     JWT secret (default: from config)
  --help, -h            Show this help message

Examples:
  # Generate token with default settings
  node generate-jwt-tokens.js

  # Generate admin token
  node generate-jwt-tokens.js --username admin --roles "ROLE_USER,ROLE_ADMIN"

  # Generate token valid for 1 hour
  node generate-jwt-tokens.js --username testuser --expiry 1

  # Generate multiple test tokens
  node generate-jwt-tokens.js --username user1 --roles "ROLE_USER"
`);
    process.exit(0);
}

// Configuration (matches application.yml)
const JWT_SECRET = getArg('--secret', 'your-super-secret-key-change-in-production-min-256-bits');
const JWT_EXPIRATION_HOURS = parseInt(getArg('--expiry', '24'));
const USERNAME = getArg('--username', 'testuser');
const ROLES = getArg('--roles', 'ROLE_USER').split(',').map(r => r.trim());

/**
 * Base64 URL encode (remove padding and make URL-safe)
 */
function base64urlEncode(str) {
    return Buffer.from(str)
        .toString('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

/**
 * Create HMAC-SHA512 signature
 */
function createSignature(data, secret) {
    return crypto
        .createHmac('sha512', secret)
        .update(data)
        .digest('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

/**
 * Generate JWT token compatible with CallCard microservice
 */
function generateJwtToken(username, roles, expirationHours = 24) {
    const now = Date.now();
    const expirationTime = now + (expirationHours * 60 * 60 * 1000);

    // JWT Header (HS512 algorithm)
    const header = {
        alg: 'HS512',
        typ: 'JWT'
    };

    // JWT Payload (matches JwtTokenProvider.java)
    const payload = {
        sub: username,
        roles: roles,
        iat: Math.floor(now / 1000),
        exp: Math.floor(expirationTime / 1000)
    };

    // Encode header and payload
    const encodedHeader = base64urlEncode(JSON.stringify(header));
    const encodedPayload = base64urlEncode(JSON.stringify(payload));

    // Create signature
    const data = `${encodedHeader}.${encodedPayload}`;
    const signature = createSignature(data, JWT_SECRET);

    // Combine to create JWT
    const jwt = `${encodedHeader}.${encodedPayload}.${signature}`;

    return {
        token: jwt,
        header: header,
        payload: payload,
        expiresAt: new Date(expirationTime).toISOString(),
        expiresIn: `${expirationHours} hours`
    };
}

/**
 * Decode JWT token to inspect claims
 */
function decodeJwtToken(token) {
    const parts = token.split('.');
    if (parts.length !== 3) {
        throw new Error('Invalid JWT format');
    }

    const header = JSON.parse(Buffer.from(parts[0].replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString());
    const payload = JSON.parse(Buffer.from(parts[1].replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString());

    return { header, payload };
}

/**
 * Generate multiple test tokens for different scenarios
 */
function generateTestTokens() {
    console.log('\n==============================================');
    console.log('   CallCard JWT Token Generator');
    console.log('==============================================\n');

    const tokens = [
        { name: 'Regular User Token', username: USERNAME, roles: ROLES },
        { name: 'Admin Token', username: 'admin', roles: ['ROLE_USER', 'ROLE_ADMIN'] },
        { name: 'Service Account Token', username: 'service-account', roles: ['ROLE_SERVICE'] },
        { name: 'Short-lived Token (1 hour)', username: USERNAME, roles: ROLES, expiry: 1 }
    ];

    tokens.forEach((config, index) => {
        console.log(`\n${index + 1}. ${config.name}`);
        console.log('─'.repeat(50));

        const result = generateJwtToken(
            config.username,
            config.roles,
            config.expiry || JWT_EXPIRATION_HOURS
        );

        console.log(`Username:    ${config.username}`);
        console.log(`Roles:       ${config.roles.join(', ')}`);
        console.log(`Expires:     ${result.expiresAt}`);
        console.log(`Valid for:   ${result.expiresIn}`);
        console.log(`\nToken:`);
        console.log(result.token);

        // Verify token can be decoded
        try {
            const decoded = decodeJwtToken(result.token);
            console.log(`\n✅ Token validated (can be decoded)`);
        } catch (err) {
            console.log(`\n❌ Token validation failed: ${err.message}`);
        }
    });

    console.log('\n==============================================');
    console.log('   How to Use These Tokens');
    console.log('==============================================\n');
    console.log('1. Copy one of the tokens above');
    console.log('2. In Postman:');
    console.log('   - Open Environment settings (eye icon)');
    console.log('   - Edit your environment');
    console.log('   - Paste token in "jwt_token" variable');
    console.log('   - Save');
    console.log('');
    console.log('3. In cURL:');
    console.log('   curl -H "Authorization: Bearer YOUR_TOKEN" \\');
    console.log('     http://172.17.165.60:8080/callcard/rest/callcards');
    console.log('');
    console.log('4. Test token validity:');
    console.log('   https://jwt.io/ (paste token to decode and verify)');
    console.log('\n==============================================\n');
}

/**
 * Generate custom token from command line arguments
 */
function generateCustomToken() {
    console.log('\n==============================================');
    console.log('   Custom JWT Token Generator');
    console.log('==============================================\n');

    const result = generateJwtToken(USERNAME, ROLES, JWT_EXPIRATION_HOURS);

    console.log(`Username:    ${USERNAME}`);
    console.log(`Roles:       ${ROLES.join(', ')}`);
    console.log(`Expires:     ${result.expiresAt}`);
    console.log(`Valid for:   ${result.expiresIn}`);
    console.log(`\nToken:`);
    console.log(result.token);

    console.log('\n─'.repeat(50));
    console.log('Token Details:');
    console.log('─'.repeat(50));
    console.log('Header:', JSON.stringify(result.header, null, 2));
    console.log('\nPayload:', JSON.stringify(result.payload, null, 2));

    console.log('\n==============================================\n');
}

// Main execution
if (args.length > 0 && !args.includes('--help')) {
    generateCustomToken();
} else {
    generateTestTokens();
}
