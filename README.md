# HTTP Server from Scratch in Java

An HTTP/1.1 server written in plain Java on top of raw TCP sockets. It uses no frameworks, no `com.sun.net.httpserver`, no Spring, no Netty, just `ServerSocket`, byte streams and the HTTP spec.

To show it working with a real browser, the repo also includes a small CRUD app called **Growth Tracker**: a static HTML/JS frontend plus a JSON API backed by PostgreSQL. The app exists to exercise the server. The server is the main part of the project. AI was used to help me research and fix bugs or fill in gaps in my knowledge and understand things under the hood better. 

## Quick start

You need Docker installed.

```
docker compose up --build
```

Open http://localhost:8080.

## How a request flows through the server

```
Browser
   │  raw bytes over TCP
   ▼
HttpServer        accepts the connection, hands it to a thread pool
   │
   ▼
handleClient      reads bytes until the end of the headers (\r\n\r\n)
   │
   ▼
HttpRequestParser validates the request line, parses headers, Content-Length
   │
   ▼
read body         fixed-length (Content-Length) or chunked
   │
   ▼
Router            finds the Handler for (method, path)
   │
   ▼
Handler           your code: returns an HttpResponse
   │
   ▼
ResponseWriter    writes status line + headers + body back to the socket
```

The connection then stays open for the next request (keep-alive) unless the client asked to close it.

## Features

**Core HTTP**
- Request line parsing and validation (`METHOD /path HTTP/1.1`)
- Supports `HTTP/1.1` and `HTTP/1.0`; other versions get `505`
- Methods: `GET`, `POST`, `PUT`, `DELETE`, `HEAD`; unknown methods get `501`
- Case-insensitive header names
- Query strings (`/goal?id=5`)

**Request bodies**
- `Content-Length` bodies, including the case where part of the body arrived in the same read as the headers
- `Transfer-Encoding: chunked`, including chunk extensions and trailers

**Connections**
- Thread pool with 20 worker threads, so many clients are served at once
- Keep-alive: several requests over one TCP connection
- `Connection: close` support
- Idle connections time out after 10 seconds
- Graceful shutdown: on Ctrl+C or `docker stop`, the server stops accepting new connections and waits for running requests to finish

**Routing**
- Register handlers per method and path
- `HEAD` falls back to the `GET` handler and sends headers only
- Wrong method on an existing path returns `405` with an `Allow` header
- Unknown path returns `404`
- Serves static files (HTML, CSS, JS) with the right `Content-Type`

**Protection against bad or malicious requests**

| Attack / bad input                                 | Response |
|----------------------------------------------------|----------|
| Malformed request line or header                   | `400`    |
| Body larger than 1 MB                              | `413`    |
| More than 100 headers                              | `431`    |
| Invalid or conflicting `Content-Length`            | `400`    |
| `Content-Length` and `Transfer-Encoding` together (request smuggling) | `400` |
| Unsupported `Transfer-Encoding`                    | `501`    |
| Malformed or oversized chunks                      | `400` / `413` |
| Body shorter than `Content-Length`                 | `400`    |

## Using the framework

A handler is any function that takes an `HttpRequest` and returns an `HttpResponse`:

```java
Router router = new Router();

router.register("GET", "/hello", request ->
    HttpResponse.text(StatusCodeEnum.OK, "Hello, world!")
);

HttpServer server = new HttpServer(8080, router);
server.start();
```

## Project structure

```
framework/               the HTTP server (the main part)
  services/
    HttpServer.java          sockets, thread pool, keep-alive, body reading
    HttpRequestParser.java   request line, headers, Content-Length, limits
    ResponseWriter.java      turns an HttpResponse into bytes
    StaticFileService.java   loads frontend files
  handlers/
    Router.java              (method, path) → Handler
    Handler.java             functional interface for request handlers
    StaticFileHandler.java   serves HTML/CSS/JS
  entities/                  HttpRequest, HttpResponse, status codes, content types
  exceptions/                HttpParseException (carries the status code to send)

growth_tracker/          demo app built on the framework
  controllers/ services/ repositories/ dtos/ entities/ config/

frontend/                index.html, app.js, style.css
init.sql                 database schema
lib/                     Jackson (JSON) and the PostgreSQL JDBC driver
```

## Demo app API

| Method   | Path           | Description         |
|----------|----------------|---------------------|
| `GET`    | `/goals`       | List all goals      |
| `GET`    | `/goal?id=1`   | Get one goal        |
| `POST`   | `/goal`        | Create a goal (JSON body: `title`, `dailyTarget`) |
| `DELETE` | `/goal?id=1`   | Delete a goal       |

Try it with curl:

```
curl http://localhost:8080/goals
curl -X POST http://localhost:8080/goal -H "Content-Type: application/json" -d "{\"title\":\"Read\",\"dailyTarget\":\"20 pages\"}"
curl -X DELETE "http://localhost:8080/goal?id=1"
```

## Running without Docker (for development)

The project has no Maven or Gradle; the dependencies are the jars in `lib/`.

1. Start only the database:
   ```
   docker compose up -d postgres
   ```
2. Compile (PowerShell, Windows):
   ```
   javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse -Filter *.java).FullName
   ```
3. Run from the project root, so `frontend/` can be found:
   ```
   java -cp "out;lib/*" Main
   ```
   On Linux/macOS, use `:` instead of `;` in the classpath.

The database connection can be configured with the `DB_URL`, `DB_USER` and `DB_PASSWORD` environment variables. Without them, it uses `localhost:5432`.

## Not implemented

This is a learning project, not a production server. It does not support:
- HTTPS/TLS
- HTTP/2
- Compression (gzip)
- `Expect: 100-continue`
- A limit on total header size (only the header count is limited)
- Unit tests (To be added soon)
