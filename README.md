## Bank Management System (Java + PostgreSQL)

A console-based backend banking application built using Java (JDBC) and PostgreSQL.
Supports core banking operations with transaction-safe database handling.

## Features

* Create Customer & Account

* Deposit Money

*  Withdraw Money

* Check Balance

* View Transaction History

* ACID-compliant operations using commit/rollback

* Relational database with foreign keys

## Tech Stack

Language: Java

Database: PostgreSQL

Connectivity: JDBC

Concepts: OOP, SQL, Transactions, ACID


## Core Workflow

* Account Creation

* Insert customer details

* Generate customer_id

* Create account linked to customer

* Auto-generate unique account number

* Deposit / Withdraw

* Validate account

* Record transaction

* Update balance

* Commit changes (rollback on failure)

## How to Run

Install PostgreSQL and create database

Create tables (Customers, Accounts, Transactions)

Configure DB connection in DBConnection.java

String URL = "jdbc:postgresql://localhost:5432/banksystem_db";
String USER = "postgres";
String PASSWORD = "your_password";


Add PostgreSQL JDBC driver (.jar) to project

Run Main.java

## Sample Menu
1. Create Account
2. Check Balance
3. Deposit
4. Withdraw
5. Transaction History
6. Exit

## Key Concepts Demonstrated

JDBC PreparedStatement usage

Foreign Key relationships

Transaction management (commit/rollback)

Sequence-based unique account number generation

Error handling and data consistency

