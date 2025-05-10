# SHOP APP

## Overview

SHOP APP is a reactive application consisting of three microservices, using Redis for caching and PostgreSQL for data storage. The application provides efficient purchase management and offers flexible deployment options using either Docker or Maven.

## Project Architecture

The project consists of the following components:

1. **Main Service** - core service for managing products, carts, and orders
   - Port: 8080
   - Dependencies: PostgreSQL, Redis, Auth Server

2. **Payment Service** - service for processing payments
   - Port: 8081
   - Dependencies: PostgreSQL, Auth Server

3. **Auth Server** - OAuth2 authorization server for authentication and authorization
   - Port: 9000

4. **PostgreSQL** - database for storing data
   - Port: 5432
   - Database: shop_db

5. **Redis** - storage for data caching
   - Port: 6379

## User Accounts

The application comes with predefined user accounts for testing:

- Regular User:
  - Username: user
  - Password: password
  - Roles: USER

- Administrator:
  - Username: admin
  - Password: admin!
  - Roles: ADMIN

## Requirements

- JDK 21 or higher
- Docker and Docker Compose (for Docker deployment)
- Maven (for Maven deployment)
- PostgreSQL (for Maven deployment)
- Redis (for Maven deployment)

## Running with Docker

### Step 1: Ensure Docker is Installed

Verify that Docker and Docker Compose are properly installed on your system.

### Step 2: Unpack the Archive

Extract the application archive to your desired location and navigate to that directory.

### Step 3: Launch the Application

Run the following command to build and start the Docker containers:

```bash
docker-compose up
```

This will:
- Build three images: `main_service`, `payment`, and `auth_server`
- Start the application containers
- Set up and initialize the PostgreSQL database
- Set up and initialize the Redis cache
- Configure networking between containers

The Main Service will be available at [http://localhost:8080](http://localhost:8080).
The Payment Service will be available at [http://localhost:8081](http://localhost:8081).
The Auth Server will be available at [http://localhost:9000](http://localhost:9000).

The PostgreSQL database will be accessible on port 5432.
The Redis cache will be accessible on port 6379.

### Step 4: Stop the Containers

To stop all containers and clean up resources:

```bash
docker-compose down
```

## Running with Maven

### Step 1: Unpack the Archive

Extract the application archive to your desired location.

### Step 2: PostgreSQL Setup

Ensure you have PostgreSQL running on port 5432 with a database named `shop_db` already created.

### Step 3: Redis Setup

Ensure you have Redis running on port 6379.

### Step 4: Build and Run the Microservices

Make sure Maven and JDK 21 are installed, then execute:

#### For Auth Server:
```bash
cd auth_server
./mvnw clean spring-boot:run
```

The Auth Server will start and be available at [http://localhost:9000](http://localhost:9000).

#### For Main Service:
```bash
cd main_service
./mvnw clean spring-boot:run
```

The Main Service will start and be available at [http://localhost:8080](http://localhost:8080).

#### For Payment Service:
```bash
cd payment
./mvnw clean spring-boot:run
```

The Payment Service will start and be available at [http://localhost:8081](http://localhost:8081).

**Note:** All microservices will only start successfully if there is a PostgreSQL database running on port 5432 with a database named `shop_db`. For the Main Service, a running Redis instance on port 6379 is also required. Furthermore, both Main Service and Payment Service require the Auth Server to be running.

## Usage

After starting the application, navigate to [http://localhost:8080](http://localhost:8080) in your web browser to access the SHOP APP dashboard.

You can log in using one of the predefined accounts:
- Regular user: `user` / `password`
- Administrator: `admin` / `admin!`

## API Documentation

- Main Service API: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Payment Service API: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- Auth Server Endpoints:
  - Token Endpoint: [http://localhost:9000/oauth2/token](http://localhost:9000/oauth2/token)
  - JWK Set Endpoint: [http://localhost:9000/oauth2/jwks](http://localhost:9000/oauth2/jwks)
