# Utilities

A collection of reusable utility classes and helper methods for Java applications.

This library provides lightweight utilities designed to simplify common development tasks such as safe casting, string handling, Base64 encoding, cryptographic hashing, high-performance file reading, and reflective access. Many utilities internally use caching where appropriate to improve performance and reduce repeated computation.

---

## Features

- High-performance file reading via memory-mapped IO with automatic caching
- Base64 encoding and decoding with byte[] and String convenience methods
- Cryptographic hashing (SHA-256, SHA-512, etc.) with hex output
- HMAC computation with Base64 output
- Constant-time hash verification to prevent timing side-channel attacks
- Cached MessageDigest and Mac prototypes to avoid repeated provider lookups
- Hex encoding and decoding
- Reflective class instantiation via constructor resolution
- Reflective field reading and writing with type validation
- Reflective method invocation with accessibility handling
- Safe type casting with null handling
- Inline collection and map initialization helpers
- Thread-safe string transformations with built-in caching (title-casing, delimiter stripping, camelCase expansion)
- String null/blank checking and key-value pair formatting
- Lightweight utility classes with no runtime dependencies
- Designed for modern Java (Java 21+)
- Performance-focused with internal caching

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

---

## Utility Classes

| Class | Description |
|---|---|
| `UtilFile` | Memory-mapped file reading with automatic cache invalidation |
| `UtilBase64` | Base64 encoding and decoding for byte[] and String |
| `UtilHash` | Cryptographic hashing, HMAC, hex encoding, and constant-time verification |
| `UtilClass` | Reflective class instantiation via constructor resolution |
| `UtilField` | Reflective field reading and writing with type validation |
| `UtilMethod` | Reflective method invocation with accessibility handling |
| `UtilJava` | Safe casting and inline collection/map initialization |
| `UtilString` | Thread-safe string transformations (title-casing, delimiter stripping, camelCase expansion) with caching |
