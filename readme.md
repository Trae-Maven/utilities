# Utilities

A collection of reusable utility classes and helper methods for Java applications.

This library provides lightweight utilities designed to simplify common development tasks such as safe casting, string handling, Base64 encoding, cryptographic hashing, and other common operations. Many utilities internally use caching where appropriate to improve performance and reduce repeated computation.

---

## Features

- Base64 encoding and decoding with byte[] and String convenience methods
- Cryptographic hashing (SHA-256, SHA-512, etc.) with hex output
- HMAC computation with Base64 output
- Constant-time hash verification to prevent timing side-channel attacks
- Hex encoding and decoding
- Safe type casting with null handling
- Inline collection and map initialization helpers
- String null/blank checking
- Lightweight utility classes
- Designed for modern Java (Java 21+)
- Minimal dependencies
- Performance-focused utilities
- Internal caching for frequently accessed operations
- Designed for reuse across multiple projects

---

## Requirements

Utilities has no external runtime dependencies.

The following is only needed at compile time for annotation processing:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.36</version>
    <scope>provided</scope>
</dependency>
```

---

## Installation

Add the dependency to your Maven project:
```xml
<dependency>
    <groupId>io.github.trae</groupId>
    <artifactId>utilities</artifactId>
    <version>0.0.1</version>
</dependency>
```
