# AEM Custom Components Architecture & Reference Guide

This guide provides a comprehensive technical overview of the custom AEM components developed in the **Weekend Project**. It explains how each component operates from dialog authoring to frontend rendering, compares them with **AEM Core Components**, and details advanced server/client integration patterns.

---

## 1. Custom Components Core Architecture

The diagram below illustrates the unified MVC-based execution flow shared by all standard custom components in this project:

![Custom Components Architecture Flow Diagram](file:///c:/Users/Project1/weekend/custom_components_flow.png)

### Core Lifecycle Steps:
1. **Configuration**: The author enters properties in the dialog. AEM saves these under the JCR resource node.
2. **Adaptation**: During page request, AEM adapts the JCR resource to its corresponding Java Sling Model class in RAM.
3. **Parsing**: The Sling Model processes child resources, resolves logic, and exposes getters to the HTL template.
4. **Rendering**: The HTL (Sightly) view reads from the Sling Model, constructing dynamic HTML on-the-fly.
5. **Presentation**: The browser loads CSS layouts and JS clientlibs for interactive animations and styles.

---

## 2. Detailed Component Reference

### A. Custom Tabs (`customtabs`)
*   **Location**: [customtabs](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/customtabs)
*   **Sling Model**: [CustomTabsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/CustomTabsModel.java)
*   **Properties Stored**:
    *   `tabs` (Multifield child resource): Stores individual list elements containing `tabTitle` and `tabContent`.
*   **How it Works**:
    1. The HTL template loops over `model.tabs` twice: once to draw headers with `data-tab-index`, and once to draw contents.
    2. The JS script ([customtabs.js](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/customtabs/clientlibs/js/customtabs.js)) binds click event listeners to toggle the `active` styling classes.
*   **Related Core Component**: `core/wcm/components/tabs/v1/tabs`
*   **Core vs. Custom Comparison**:
    *   *Core Tabs*: Acts as a layout container. Authors drag-and-drop entirely separate components inside each tab.
    *   *Custom Tabs*: Maps a text-based composite multifield list. Ideal for simple text articles.

---

### B. Child Page List (`childpagelist Component`)
*   **Location**: [childpagelist Component](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/childpagelist%20Component)
*   **Sling Model**: [ChildPageListModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/ChildPageListModel.java)
*   **Properties Stored**:
    *   `parentPath` (Pathbrowser): Path to the parent page.
    *   `limit` (Integer): Maximum number of sub-pages.
*   **How it Works**:
    1. The Sling Model adapts `ResourceResolver` to AEM's `PageManager` API.
    2. It calls `parentPage.listChildren()` to traverse sub-pages.
    3. It extracts Title, Description, and Path, storing them as a list of `PageItem` objects rendered by HTL.
*   **Related Core Component**: `core/wcm/components/list/v2/list`

---

### C. Character Panel (`characterpanel`)
*   **Location**: [characterpanel](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/characterpanel)
*   **Sling Model**: [CharacterPanelModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/CharacterPanelModel.java)
*   **Properties Stored**:
    *   `characters` (Multifield child resource): Stores hero profiles (`characterName`, `realName`, `fileReference`).
*   **How it Works**:
    1. The model retrieves the `./characters` node list.
    2. The HTL loops over the list, reading properties directly via `${item.properties.characterName}`.

---

### D. Team Gallery (`teamgallery`)
*   **Location**: [teamgallery](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/teamgallery)
*   **Sling Model**: [TeamGalleryModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/TeamGalleryModel.java)
*   **Properties Stored**:
    *   `galleryTitle` and `members` multifield list.
*   **How it Works**:
    1. The model maps `./members` directly into a typed `List<MemberItem>` array in RAM.
    2. HTL loops through the members list to construct a visual profile grid of cards.

---

### E. Card Component (`card`)
*   **Location**: [card](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/card)
*   **Properties Stored**: `title` and `textarea`.
*   **How it Works**:
    *   This is an **HTML-only component** (no Java backing class).
    *   HTL reads variables directly from the JCR properties map: `${properties.title}`.

---

### F. Mock API Direct (`mockapi`)
*   **Location**: [mockapi](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapi)
*   **Sling Model**: [MockApiModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiModel.java)
*   **Properties Stored**: `apiEndpoint` (String), `limit` (Integer).
*   **How it Works**:
    1. When AEM receives the page request, the Sling Model is instantiated.
    2. The Model calls the API directly from the server using Java `HttpClient`.
    3. Jackson parses and recursively flattens the JSON response, and HTL compiles the cards completely server-side.
    4. Optional clientlib JS ([mockapi.js](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapi/clientlibs/js/mockapi.js)) handles input text search filtering locally.

---

### G. Mock API Servlet Proxy (`mockapiproxy`)
*   **Location**: [mockapiproxy](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapiproxy)
*   **Servlet**: [MockApiProxyServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiProxyServlet.java)
*   **Properties Stored**: `title` (String), `apiEndpoint` (String), `limit` (Integer).
*   **How it Works**:
    1. AEM immediately serves the page HTML containing a loading spinner.
    2. Once loaded, clientlib JS ([mockapiproxy.js](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapiproxy/clientlibs/js/mockapiproxy.js)) triggers an AJAX fetch request to the local servlet path.
    3. The servlet queries the external API, limits the records, and returns JSON.
    4. Browser JS parses the JSON, flattens it, and dynamically injects the HTML cards.

---

### H. Mock API Loopback (`mockapinojs`)
*   **Location**: [mockapinojs](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapinojs)
*   **Servlet**: [MockApiNoJsServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiNoJsServlet.java)
*   **Sling Model**: [MockApiNoJsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiNoJsModel.java)
*   **Properties Stored**: `title` (String), `apiEndpoint` (String), `limit` (Integer).
*   **How it Works**:
    1. During page load, the Sling Model is instantiated.
    2. The Model copies auth cookies and triggers an internal loopback GET request to its associated Sling Servlet.
    3. The servlet retrieves raw mock API data, applies JCR limits, and responds with JSON.
    4. The Sling Model captures this JSON response, flattens it, and exposes it to [mockapinojs.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapinojs/mockapinojs.html) to render cards server-side with zero client JS.

---

## 3. Integration Patterns request Flow Comparison

Below is the visual overview comparing the request flow and rendering cycles of the three integration patterns:

![AEM Integration Architecture Flows](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/api_flows_simplified_1780486859793.png)

### The Three Flows Step-by-Step:

#### Flow 1: Direct Sling Model (`mockapi`)
*   **Step 1: Dialog Config** — Author saves the API URL and Limit in JCR.
*   **Step 2: Sling Model Binds** — Page load instantiates the Sling Model.
*   **Step 3: Java Fetch** — The Sling Model calls the API directly from the server.
*   **Step 4: JSON Parse** — The Sling Model flattens the returned JSON data.
*   **Step 5: HTL Rendering** — HTL generates the HTML cards.
*   **Step 6: HTML Response** — Complete HTML is sent directly to the Browser.

#### Flow 2: Browser JS + Servlet Proxy (`mockapiproxy`)
*   **Step 1: Page Load** — AEM sends the HTML page shell with a loading spinner.
*   **Step 2: JS Trigger** — The component's Clientlib JS triggers a background fetch to AEM's local Servlet.
*   **Step 3: Servlet Query** — The local AEM Servlet reads JCR configurations and queries the External API.
*   **Step 4: JSON Response** — The Servlet returns the limited JSON back to the browser's JS.
*   **Step 5: JS Render** — Browser JS flattens the JSON and injects the HTML cards dynamically.

#### Flow 3: No-JS Loopback (`mockapinojs`)
*   **Step 1: Model Binds** — The Sling Model binds on page request.
*   **Step 2: Loopback Fetch** — The Sling Model copies credentials and requests the local AEM Servlet URL internally.
*   **Step 3: Servlet Query** — The Servlet reads JCR configs, calls the External API, and retrieves raw data.
*   **Step 4: JSON Response** — The Servlet returns the JSON stream to the Sling Model.
*   **Step 5: JSON Parse** — The Sling Model flattens the loopback JSON.
*   **Step 6: HTL Rendering** — HTL compiles the HTML cards server-side.
*   **Step 7: HTML Response** — The Browser receives the completed HTML directly; no client-side JS is used.

---

## 4. Core AEM Java Concepts: Models vs. Services vs. Servlets

*   **Sling Model (Component Backend):**
    *   **Definition:** Binds to JCR resources/requests to map authored configurations (`@ValueMapValue`) directly to Java fields.
    *   **Lifecycle:** Short-lived. Instantiated on page load and destroyed right after rendering.
    *   **When to use:** To process and expose content properties specifically for HTL rendering.
*   **OSGi Service (Global Engine):**
    *   **Definition:** Central Java class containing business logic that operates as a singleton.
    *   **Lifecycle:** Long-lived. Remains in AEM's memory as long as AEM is active.
    *   **When to use:** For shared, reusable operations (e.g. database calls, integrations, caching).
*   **Sling Servlet (Web Gateway):**
    *   **Definition:** An HTTP endpoint mapped to paths or resource types to process requests (`GET`, `POST`).
    *   **Lifecycle:** Request-bound. Active only during request processing.
    *   **When to use:** To expose endpoints to browser AJAX requests or handle external webhook integrations.

---

## 5. Component Boilerplate & File Configurations

*   **Component Registration (`.content.xml`):**
    Sets display name and category group.
    ```xml
    <jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0"
        jcr:primaryType="cq:Component"
        jcr:title="Component Title"
        componentGroup="Weekend Project - Content"/>
    ```
*   **Dialog Configuration (`_cq_dialog/.content.xml`):**
    Configures standard touch-UI dialog panels. Note the `./` prefix mapping:
    ```xml
    <apiEndpoint
        jcr:primaryType="nt:unstructured"
        sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
        fieldLabel="API Endpoint URL"
        name="./apiEndpoint"/>
    ```
*   **HTL Render Template (`component.html`):**
    Binds models and loads CSS/JS client libraries.
    ```html
    <sly data-sly-use.model="com.weekend.core.models.MockApiModel" />
    <sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html">
        <sly data-sly-call="${clientlib.all @ categories='weekend.components.mockapi'}" />
    </sly>
    ```

---

## 6. Comparison Matrix: Core Components vs. Custom Components

| Requirement / Use Case | Core Components | Custom Components |
| :--- | :--- | :--- |
| **Grid / Layout Structure** | Flexible, columns edited directly in browser. | Predefined, rigid, guaranteed layout styles. |
| **Content Nesting** | Allows nesting full components inside panels. | Restricts content to simple fields (text, paths). |
| **Authoring Speed** | Slower (requires dropping multiple components). | Faster (configured via a single dialog form). |
| **Complexity** | High (involves container policy configurations). | Low (self-contained logic). |
