
---

# WebClient Service

This repository contains a Spring Boot-based WebClient service used to send HTTP requests to interact with the Task Management system. This service acts as a client application that integrates with the Task Management project to perform various operations such as fetching tasks, retrieving user details, and handling failures gracefully.

## Features

- **Task Management API Integration**:
    - The service sends HTTP requests to the Task Management project, enabling:
        - Fetching all tasks.
        - Retrieving task details by ID.
    - Uses **Spring WebClient** for asynchronous and non-blocking HTTP communication.

- **Resilience4j Integration**:
    - Implements circuit breaker functionality to handle potential failures when interacting with the Task Management API. If a failure occurs (e.g., a service is down), the circuit breaker triggers a fallback method, ensuring that the application doesn't get overwhelmed with retries.
    - Supports configurable circuit breaker settings, including failure thresholds and time windows.

- **Logging**:
    - Comprehensive logging using **SLF4J** and **Logback** to record all requests and responses.
    - Logs are saved based on severity levels (INFO, ERROR) and are split into daily files organized by date. The file structure ensures easy traceability and maintenance.
 
      For example:
        - `logs/2024-09/05-09_info_webclient-service.log`
        - `logs/2024-09/05-09_error_webclient-service.log`

- **Error Handling**:
    - Implements robust error handling for WebClient requests, including fallbacks for common network-related exceptions (like `WebClientRequestException` or `ConnectException`).
    - Each error is logged for debugging and troubleshooting.

- **Caching**:
    - Instead of simply showing a custom error message when the server is down and the Circuit Breaker is triggered, the service uses caching to offer a more user-friendly experience. When the Task Management API is unavailable, previously cached data (such as task lists) is returned to the user. This approach ensures continuity and a better user experience, even during outages, by delivering the most recent cached version of the data.

---
## Project Structure

```
webclient-service/
├── .gitignore               # Specifies ignored files for Git
├── .idea/                   # Project files for IntelliJ IDEA
├── .mvn/                    # Maven wrapper files
├── HELP.md                  # Basic help file
├── logs/                    # Directory for application logs
├── mvnw, mvnw.cmd           # Maven wrapper scripts
├── pom.xml                  # Maven project descriptor (dependencies, plugins, etc.)
├── README.md                # Project documentation
├── src/                     # Source code folder
│   ├── main/                # Main source code
│   │   ├── java/            # Java source files
│   │   ├── resources/       # Configuration files and static resources
│   └── test/                # Test cases for the project
└── target/                  # Compiled output (generated by Maven)
```
---

## Dependencies

The project leverages the following key dependencies:

- **Spring Boot**: The core framework for building the application.
- **Spring WebFlux**: For using WebClient to send reactive HTTP requests.
- **Resilience4j**: For implementing the circuit breaker and fallback mechanisms.
- **SLF4J and Logback**: For logging and managing logs efficiently.
- **SpringDoc OpenAPI**: For generating API documentation.
- **Lombok**: To reduce boilerplate code in model classes.
- **Reactor Test**: For writing and running reactive tests.



## How It Works

1. **Fetching Tasks**: The service fetches tasks from the Task Management project using the `/task-api/list-tasks` endpoint.
2. **Fallback Method**: In case of failures (e.g., network issues or the API being unavailable), the circuit breaker triggers and directs to a fallback method that provides default responses to avoid system crashes.
3. **Logging**: All requests and responses are logged for monitoring purposes. The logs are categorized by severity (INFO, ERROR) and stored by date.
4. **Caching**: Tasks are cached to reduce response time and improve efficiency for frequently accessed data.

## Getting Started

### Prerequisites

- **Java Version**: 17
- **Maven Version**: 3.6+
- Access to the Task Management API

### Running the Service

1. Clone the repository:
   ```bash
   git clone https://github.com/YasinsSE/webclient-service.git
   ```
2. Navigate to the project directory and build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```


## Configuration

### API URL

Update the base URL for the Task Management API in the `application.yaml` or `application.properties` file:
```yaml
taskmanager.api.url: http://localhost:8080/task-api
```



## Contributing

Contributions are welcome! Feel free to submit issues and pull requests to improve the project.

---