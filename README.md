# Homework 1: Hash Functions

## Overview

In this assignment, you will implement and experiment with hash function based applications using SHA-256, and some variants, to understand three key properties that make hash functions secure:

1. **Collision Resistance**: It should be hard to find two different inputs with the same hash
2. **Hiding**: Given a hash, it should be hard to find the original input
3. **Puzzle Friendliness**: There should be no shortcut better than brute force for solving hash puzzles

## Learning Objectives

By completing this assignment, you will:
- Implement message digests for integrity verification
- Build a commitment scheme using cryptographic hashing
- Create a proof-of-work puzzle solver
- Empirically measure how hash output length affects the three properties above
- Understand the computational difficulty of breaking each security property

## What You'll Build

### Part 1: Message Digests
Implement hash-based integrity checking and test collision resistance by attempting to find two messages with the same digest.

### Part 2: Commitment Schemes
Build a cryptographic commitment scheme and test the hiding property by attempting to reverse engineer committed messages.

### Part 3: Puzzle Friendliness
Implement a proof-of-work puzzle solver (like Bitcoin mining) and measure how difficulty scales with the target set size.

## Provided Files

- **Utils.java**: Contains helper functions including:
  - `sha256()`: Compute full SHA-256 hash
  - `hashTruncated()`: Compute SHA-256 truncated hash with specified bit length. You will use 8 and 16 for the experiments.
  - `concat()`: Concatenate byte arrays
  - `genSalt()`: Generate random 32-byte salt


- **Commitment.java**: Commitment class

- **Template files** (use these as starting points):
  - `Part1.java`: Message digest implementation
  - `Part2.java`: Commitment scheme implementation
  - `Part3.java`: Puzzle solver implementation
  - Do not change any of the method signatures in this file to ensure compatability with the autograder.

## Deliverables

For each part, you will submit:
1. **Code**: Completed implementations of Part1.java, Part2.java, and Part3.java as well as your Driver code to complete the experiments.
2. **Experimental results**: Completed data tables

Submit to Gradescope.

# Part 1: Message Digests

A **message digest** (or cryptographic hash) is a fixed-length "fingerprint" of a message or file. It serves as an unambiguous summary that can verify the message hasn't been tampered with.

**Example use case:** Uploading/downloading files from the cloud
```
1. Upload file → compute digest: d = H(file)
2. Store digest locally 
3. Later, download file → recompute: d' = H(downloaded_file)
4. Compare: if d == d', file is unchanged
           if d != d', file was tampered with or corrupted
```

## Part 1A: Computing Message Digests


### Task 1: Implement `computeDigest()`
```java
/**
 * Compute the message digest of a message using specified hash function.
 * 
 * @param message the message to hash
 * @param hashFunction which hash to use:
 *                     1 = full SHA-256 (256 bits)
 *                     2 = SHA-256 truncated to 8 bits
 *                     3 = SHA-256 truncated to 16 bits
 *                     other = print error and return null
 * @return the message digest as a byte array (or long for truncated versions)
 */
public static byte[] computeDigest(byte[] message, int hashFunction) 
```

### Task 2: Implement `verifyIntegrity()`
```java
/**
 * Verify that a message matches an expected digest.
 * 
 * @param message the message to verify
 * @param expectedDigest the expected digest
 * @param hashFunction which hash function to use (1, 2, or 3)
 * @return true if message's digest matches expectedDigest
 */
public static boolean verifyIntegrity(byte[] message, byte[] expectedDigest, 
                                      int hashFunction)  {
}
```

## Part 1B: Finding Collisions

Test collision resistance by trying to find two different messages with the same digest.
Experiment with sha-256, sha-256 truncated to 8 bits, and sha-256 truncated to 16 bits using flags 1 2 and 3

**Collision results**: Create a table:

   | Hash Function | Bits | Possible Values | Expected Attempts | Actual Attempts | Time (ms) | Found? |
   |---------------|------|-----------------|-------------------|-----------------|-----------|--------|
   | 2             | 8    | 256             | 20                | 29              | 2         | Y      |
   | 3             | 16   | 65,536          | 320               | 443             | 12        | Y      |
   | 1             | 256  | 2^256           | 2^128             | ?               | 568927    | N      |


Compute the expected attempts required to find a collision using the analysis we did in class. 

# Part 2: Commitment schemes

A **commitment scheme** allows you to commit to a value without revealing it, then later reveal and verify the original value. It is the digital equivalent of putting a message in a sealed envelope.

A secure commitment scheme must satisfy two properties:
1. **Hiding**: Given only the commitment c, it should be computationally infeasible to determine the original message m
2. **Binding**: You cannot find two different messages that produce the same commitment

We construct commitments using: **c = H(r || m)** where:
- `r` is random "salt" (32 bytes from `Utils.genSalt()`)
- `m` is the message you're committing to
- `||` means concatenation (use `Utils.concat()`)
- `H` is a cryptographic hash function

---

## Part 2A: Implement the Commitment Scheme


### Task 1: Implement `commit()`
```java
public static Commitment commit(byte[] message, int hashFunction) 
```

### Task 2: Implement `verify`
```java 
public static boolean verify(Commitment c, byte[] message, int hashFunction) { . 
```

## Part 2B: Testing the Hiding Property

### Your Task: Break a Weak Hash Function

Implement a brute-force attack that tries to find a message matching a given commitment value.


### Experiments to Run 

**Hiding property results**: Create a table:
Experiment with sha-256, sha-256 truncated to 8 bits, and sha-256 truncated to 16 bits using flags 1 2 and 3

| Hash Function | Bits | Possible Values | Expected Attempts | Actual Attempts | Time (ms) | Found? |
|---------------|------|-----------------|-------------------|-----------------|-----------|--------|
| 2             | 8    | 256             | 128               | 61              | 4         | Y      |
| 3             | 16   | 65,536          | 32,768            | 57199           | 99        | Y      |
| 1             | 256  | 2^256           | 2^255             | ?               | 631752    | N      |

Compute the expected attempts required to break the hiding property using the analysis we did in class.
Include the two collliding inputs in your submission.

# Part 3: Puzzle Friendliness

A hash function is **puzzle friendly** if there is no solving strategy much better than trying random values. This property is essential for Bitcoin mining. We will learn more about what this means in future lectures. 

A **search puzzle** consists of:
- A hash function H
- A **puzzle-ID** (id) chosen from a high min-entropy distribution
- A **target set** Y

A **solution** to this puzzle is a value x such that H(id || x) ∈ Y.

### Puzzle Difficulty

The size of the target set |Y| determines the difficulty of the puzzle:
- If **Y is the set of all n-bit strings**, the puzzle is **trivial** (any value of x works)
- If **Y has only one element**, the puzzle is **maximally hard** (expected 2^n attempts)
- In general, expected attempts ≈ (2^n) / |Y|

In Bitcoin, the search puzzle is:
- **H** = SHA-256
- **id** = data from the blockchain
- **x** = nonce (the value miners search for)
- **Y** = {all hashes meeting the current difficulty requirement}

Bitcoin adjusts difficulty by changing |Y|.

--- 

## Representing Y with Leading Zeros

For this assignment, we'll define the target set Y using **leading zero bits**, which is a common and intuitive way to specify difficulty:

**Y = {all hashes with at least d leading zero bits}**

This means:
- A hash is in Y if its first d bits are all 0
- Equivalently, the hash value < 2^(256-d) when interpreted as a number

**Examples:**
- **d = 0**: Y contains all 256-bit strings → |Y| = 2^256 → trivial puzzle
- **d = 8**: Y contains hashes starting with 00000000... → |Y| = 2^248 → expected 2^8 = 256 attempts
- **d = 16**: Y contains hashes starting with 16 zeros → |Y| = 2^240 → expected 2^16 = 65,536 attempts
- **d = 256**: Y contains only one hash (all zeros) → |Y| = 1 → expected 2^256 attempts (maximally hard)

In general, with d leading zero bits:
- |Y| = 2^(256-d)
- Expected attempts = 2^256 / 2^(256-d) = 2^d

---

## Part 3A: Implement Search Puzzle Solver

### Task 1: Implement `solvePuzzle()`
```java
/**
 * Find a solution x (nonce) such that H(puzzleID || x) ∈ Y,
 * where Y is the set of all hashes with at least 'difficulty' leading zero bits.
 * 
 * @param puzzleID the puzzle identifier (arbitrary data - high min-entropy value)
 * @param difficulty number of leading zero bits required (defines |Y| = 2^(256-difficulty))
 * @return a nonce x that solves the puzzle, or -1 if not found within reasonable attempts
 */
public static long solvePuzzle(byte[] puzzleID, int difficulty) throws Exception {
    // TODO
    throw new UnsupportedOperationException("TODO");
}
```


### Task 2: Implement `verifyPuzzle()`
```java
/**
 * Verify that x is a valid solution: H(puzzleID || x) ∈ Y.
 * 
 * @param puzzleID the puzzle identifier
 * @param x the proposed solution (nonce)
 * @param difficulty required number of leading zero bits (defines Y)
 * @return true if H(puzzleID || x) has at least 'difficulty' leading zeros
 */
public static boolean verifyPuzzle(byte[] puzzleID, long x, int difficulty) 
        throws Exception {
    // TODO
    throw new UnsupportedOperationException("TODO");
}
```

## Part 3B: Testing Puzzle Friendliness

Test how long it takes to solve puzzles of different difficulties.

**Puzzle friendliness results**: Create a table:

| Difficulty (d) | Target Set Size \|Y\| | Expected Attempts      | Actual Attempts | Time (ms) | Found? |
|----------------|-----------------------|------------------------|-----------------|-----------|--------|
| 12             | 2^244                 | 4096                   | 256             | 0         | Y      |
| 16             | 2^240                 | 65,536                 | 25463           | 24        | Y      |
| 20             | 2^236                 | 1,048,576              | 249302          | 145       | Y      |
| 24             | 2^232                 | 16,777,216             | 15952377        | 5317      | Y      |


---

