# SHOP APP

## Overview

SHOP APP is a reactive application consisting of two microservices, using Redis for caching and PostgreSQL for data storage. The application provides efficient purchase management and offers flexible deployment options using either Docker or Maven.

## Project Architecture

The project consists of the following components:

1. **Main Service** - core service for managing products, carts, and orders
   - Port: 8080
   - Dependencies: PostgreSQL, Redis

2. **Payment Service** - service for processing payments
   - Port: 8081
   - Dependencies: PostgreSQL

3. **PostgreSQL** - database for storing data
   - Port: 5432
   - Database: shop_db

4. **Redis** - storage for data caching
   - Port: 6379

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
- Build two images: `main_service` and `payment`
- Start the application containers
- Set up and initialize the PostgreSQL database
- Set up and initialize the Redis cache
- Configure networking between containers

The Main Service will be available at [http://localhost:8080](http://localhost:8080).
The Payment Service will be available at [http://localhost:8081](http://localhost:8081).

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

**Note:** Both microservices will only start successfully if there is a PostgreSQL database running on port 5432 with a database named `shop_db`. For the Main Service, a running Redis instance on port 6379 is also required.

## Usage

After starting the application, navigate to [http://localhost:8080](http://localhost:8080) in your web browser to access the SHOP APP dashboard.

## API Documentation

- Main Service API: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Payment Service API: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
