# Utilities

A collection of reusable utility classes and helper methods for Java applications.

This library provides lightweight utilities designed to simplify common development tasks such as safe casting, string handling, Base64 encoding, cryptographic hashing, high-performance file reading, generic type resolution, reflective access, logging, time formatting, and generic functional types. Many utilities internally use caching where appropriate to improve performance and reduce repeated computation.

Built for modern Java (Java 21+).

---

## Features

- High-performance file reading via memory-mapped IO with automatic caching
- Base64 encoding and decoding with byte[] and String convenience methods
- Cryptographic hashing (SHA-256, SHA-512, etc.) with hex output
- HMAC computation with Base64 output
- Constant-time hash verification to prevent timing side-channel attacks
- Cached MessageDigest and Mac prototypes to avoid repeated provider lookups
- Hex encoding and decoding
- Reflective generic type parameter resolution across class hierarchies and interfaces
- Reflective class instantiation via constructor resolution
- Reflective field reading and writing with type validation
- Reflective method invocation with accessibility handling
- Safe type casting with null handling
- Inline collection and map initialization helpers
- Thread-safe string transformations with built-in caching (title-casing, delimiter stripping, camelCase expansion)
- String null/blank checking and key-value pair formatting
- Static logging wrapper around Google Flogger with configurable logger instance
- Human-readable duration formatting with best-fit unit resolution and configurable decimal precision
- Elapsed time checks against a millisecond timestamp
- Generic pair types (Pair, TriPair, QuadPair) for lightweight data grouping
- Generic function types (Function, BiFunction, TriFunction, QuadFunction)
- Generic consumer types (Consumer, BiConsumer, TriConsumer, QuadConsumer)
- Performance-focused with internal caching

---

## Requirements

The following is only needed at compile time for annotation processing and is expected to already exist in your application:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.36</version>
    <scope>provided</scope>
</dependency>
```

---

## Built-in Dependencies

Utilities includes several dependencies that are automatically included when you install the library.

- [Google Flogger](https://github.com/google/flogger) – Fluent logging API used internally by `UtilLogger`.

```xml
<dependency>
    <groupId>com.google.flogger</groupId>
    <artifactId>flogger</artifactId>
</dependency>

<dependency>
    <groupId>com.google.flogger</groupId>
    <artifactId>flogger-system-backend</artifactId>
</dependency>
```

These dependencies are automatically included when installing Utilities and do not need to be added manually.

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
| `UtilGeneric` | Reflective generic type parameter resolution with caching |
| `UtilClass` | Reflective class instantiation via constructor resolution |
| `UtilField` | Reflective field reading and writing with type validation |
| `UtilMethod` | Reflective method invocation with accessibility handling |
| `UtilJava` | Safe casting and inline collection/map initialization |
| `UtilString` | Thread-safe string transformations (title-casing, delimiter stripping, camelCase expansion) with caching |
| `UtilTime` | Human-readable duration formatting and elapsed time checks |
| `UtilLogger` | Static logging wrapper around Google Flogger with configurable logger instance |

## Data Types

| Class | Package | Description |
|---|---|---|
| `Pair` | `objects.pair` | A generic pair of two values |
| `TriPair` | `objects.pair` | A generic pair of three values |
| `QuadPair` | `objects.pair` | A generic pair of four values |

## Functional Types

| Interface | Package | Description |
|---|---|---|
| `Function` | `objects.function` | Accepts one argument, produces a result |
| `BiFunction` | `objects.function` | Accepts two arguments, produces a result |
| `TriFunction` | `objects.function` | Accepts three arguments, produces a result |
| `QuadFunction` | `objects.function` | Accepts four arguments, produces a result |
| `Consumer` | `objects.consumer` | Accepts one argument, returns no result |
| `BiConsumer` | `objects.consumer` | Accepts two arguments, returns no result |
| `TriConsumer` | `objects.consumer` | Accepts three arguments, returns no result |
| `QuadConsumer` | `objects.consumer` | Accepts four arguments, returns no result |
