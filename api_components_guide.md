# AEM Mock Integration Components - Workflow Guide

This guide explains the architecture, request flows, and key differences between the three mock API integration components developed in the Weekend Project:
1. **Mock API Direct Component** (`mockapi`)
2. **Mock API Servlet Proxy Component** (`mockapiproxy`)
3. **Mock API Server-Side Loopback Component** (`mockapinojs`)

---

## 1. Architectural Workflow Comparison

Below is the visual overview comparing the request flow and rendering cycles of all three integration patterns:

![AEM Integration Architecture Flows](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/api_flows_updated_1780486404463.png)

---

## 2. Component 1: Mock API Direct (`mockapi`)

### Description
This component performs a **Direct Server-to-Server** HTTP request. When AEM receives the page request, the Sling Model executes, queries the third-party REST API directly, flattens the JSON response, and maps it to Java objects. HTL loops through this data to render HTML before the page is delivered to the browser.

### Sequence Diagram
```mermaid
sequenceDiagram
    autonumber
    actor Browser as User Browser
    participant AEM as AEM Server (Sling Model + HTL)
    participant API as External Mock API

    Browser->>AEM: Requests page (/content/weekend/us/en.html)
    activate AEM
    Note over AEM: Sling Model instantiates & reads config
    AEM->>API: Server-to-Server GET (https://jsonplaceholder.typicode.com/users)
    activate API
    API-->>AEM: Returns Raw JSON Payload
    deactivate API
    Note over AEM: Jackson maps & flattens JSON data
    Note over AEM: HTL compiles cards into HTML
    AEM-->>Browser: Sends fully-rendered page (zero client-side loading)
    deactivate AEM
```

---

## 3. Component 2: Mock API Servlet Proxy (`mockapiproxy`)

### Description
This component uses **Client-Side Rendering (Asynchronous)**. AEM immediately serves the page HTML containing a loading spinner. Once loaded, client-side JavaScript sends an AJAX `fetch()` request to a local OSGi Servlet endpoint (`/content/.../mockapiproxy.users.json`). The servlet requests the external API, applies JCR limit configurations, and returns JSON. The browser JS then parses the JSON and builds the card DOM structure dynamically.

### Sequence Diagram
```mermaid
sequenceDiagram
    autonumber
    actor Browser as User Browser
    participant AEM_HTL as AEM Page Shell (HTML)
    participant JS as Client-Side JS (mockapiproxy.js)
    participant Servlet as OSGi Servlet (MockApiProxyServlet)
    participant API as External Mock API

    Browser->>AEM_HTL: Requests page (/content/weekend/us/en.html)
    AEM_HTL-->>Browser: Instantly sends Page Shell with Loading Spinner
    Note over Browser: Page renders main layout immediately
    Browser->>JS: Executes mockapiproxy.js on load
    activate JS
    JS->>Servlet: AJAX fetch request to local JCR selector path (.users.json)
    activate Servlet
    Note over Servlet: Reads endpoint & limit settings
    Servlet->>API: Server-to-Server GET call
    activate API
    API-->>Servlet: Returns raw JSON
    deactivate API
    Note over Servlet: Limits data & writes JSON to stream
    Servlet-->>JS: Returns limited JSON list
    deactivate Servlet
    Note over JS: JS flattens JSON keys dynamically
    Note over JS: Injects HTML cards (replaces spinner)
    deactivate JS
```

---

## 4. Component 3: Mock API Server-Side Loopback (`mockapinojs`)

### Description
This component operates **Server-Side with No JavaScript** but routes the integration through a backend **Sling Servlet**. During page compilation, the Sling Model is instantiated. Instead of calling the API directly, it performs a local loopback HTTP call internally to its associated OSGi Servlet. The servlet fetches the mock API data, applies JCR limits, and writes it out as JSON. The Sling Model captures this JSON response, flattens it, and exposes it to HTL for server-side HTML rendering.

### Sequence Diagram
```mermaid
sequenceDiagram
    autonumber
    actor Browser as User Browser
    participant AEM_HTL as AEM Page Render (HTL)
    participant Model as Sling Model (MockApiNoJsModel)
    participant Servlet as OSGi Servlet (MockApiNoJsServlet)
    participant API as External Mock API

    Browser->>AEM_HTL: Requests page (/content/weekend/us/en.html)
    activate AEM_HTL
    AEM_HTL->>Model: Instantiates Model on page load
    activate Model
    Note over Model: Constructs loopback URL (http://localhost:4502/...)
    Note over Model: Copies request's Cookies & Authorization headers
    Model->>Servlet: Loopback HTTP GET request (.users.json)
    activate Servlet
    Note over Servlet: Extracts JCR settings (apiEndpoint, limit)
    Servlet->>API: Server-to-Server GET call
    activate API
    API-->>Servlet: Returns raw JSON
    deactivate API
    Note over Servlet: Limits data & returns JSON stream
    Servlet-->>Model: Returns JSON payload
    deactivate Servlet
    Note over Model: Jackson maps & flattens JSON data
    Model-->>AEM_HTL: Returns DynamicCard lists
    deactivate Model
    Note over AEM_HTL: HTL compiles cards into HTML
    AEM_HTL-->>Browser: Sends complete HTML page (no JS execution)
    deactivate AEM_HTL
```

---

## 5. Logical & Performance Comparison Matrix

| Criteria | **Direct Component (`mockapi`)** | **Servlet Proxy Component (`mockapiproxy`)** | **Loopback Component (`mockapinojs`)** |
| :--- | :--- | :--- | :--- |
| **Rendering** | Server-Side (Java/HTL) | Client-Side (Browser JS) | Server-Side (Java/HTL) |
| **JS Required?** | **No** (Optional search box filter only) | **Yes** (Required for fetch & DOM injection) | **No** (Zero client-side JS executing) |
| **Performance Impact** | Blocks page rendering until external API responds. | Instant page load; visual cards load asynchronously. | Blocks page rendering; adds overhead of internal HTTP request. |
| **Authentication** | None needed for client. | Handled automatically by AEM resource access. | Relies on Model copying Authorization headers to Loopback. |
| **Use Case** | Search directories or SEO-heavy widgets that must load on step one. | Dashboard panels, slow/third-party widgets, non-SEO data grids. | Legacy environments requiring strict no-JS execution combined with servlet reuse. |
