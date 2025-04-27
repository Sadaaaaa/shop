# SHOP APP

## Overview

SHOP APP is a reactive application with Redis cache for managing purchases efficiently. It offers flexible deployment options using either Docker or Maven to suit your development and production needs.

## Requirements

- JDK 21 or higher
- Docker and Docker Compose (for Docker deployment)
- Maven (for Maven deployment)
- PostgreSQL (for Maven deployment)

## Running with Docker

### Step 1: Ensure Docker is Installed

Verify Docker and Docker Compose are properly installed on your system.

### Step 2: Unpack the Archive

Extract the application archive to your desired location and navigate to that location.

### Step 3: Launch the Application

Run the following command to build and start the Docker containers:

```bash
docker-compose up
```

This will:
- Build two images: `main_service` and `payment`
- Start the application containers
- Set up and initialize the database
- Set up and initialize the Redis cache
- Configure networking between containers

The application will be available at [http://localhost:8080](http://localhost:8080).

The database will be accessible on port 5432.
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

### Step 4: Build and Run the Application

Make sure Maven and JDK 21 are installed, then execute:

```bash
./mvnw clean spring-boot:run
```

The application will start and be available at [http://localhost:8080](http://localhost:8080).

**Note:** The application will only start successfully if there is a PostgreSQL database running on port 5432 with a database named `shop_db`.


## Running with the JAR File

### Step 1: Unpack the Archive

Extract the application archive to your desired location.

### Step 2: PostgreSQL Setup

Ensure you have PostgreSQL running on port 5432 with a database named `shop_db` already created.

### Step 3: Navigate to the Target Directory

Navigate to the target directory in the unpacked archive.

### Step 4: Run the JAR File

Execute the following commands to run the application directly (JVM must be installed):

```bash
mvn clean install
java -jar shop-0.0.1-SNAPSHOT.jar
```

The application will start and be available at [http://localhost:8080](http://localhost:8080).

**Note:** The application will only start successfully if there is a PostgreSQL database running on port 5432 with a database named `shop_db`.

## Usage

After starting the application, navigate to [http://localhost:8080](http://localhost:8080) in your web browser to access the SHOP APP dashboard.
