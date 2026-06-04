# AEM Custom Components Architecture & Reference Guide

This guide provides a comprehensive technical overview of the custom AEM components developed in the **Weekend Project**. It explains how each component operates from dialog authoring to frontend rendering, compares them with **AEM Core Components**, and details advanced server/client integration patterns.

---

## 1. Custom Components Core Architecture

The diagram below illustrates the unified MVC-based execution flow shared by all standard custom components in this project:

![Custom Components Architecture Flow Diagram](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/custom_components_flow_1780385105020.png)

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

#### Flow Diagram
![Custom Tabs Request and Interactive Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/custom_tabs_flow_1780287459986.png)

#### Detailed Flow Explanation:
1. **Authoring (JCR)**: The author configures tab items (titles and text contents) in the multifield dialog. AEM saves these as children under the current JCR node (`/jcr:content/root/container/customtabs/tabs/item_1`, `item_2`).
2. **Model Adaptation**: During page request, the Sling Model is instantiated and retrieves the `tabs` child resources, preparing them as a list of Java DTO objects.
3. **HTL Rendering**: The HTL template loops over this Java list twice:
   * First loop generates the tab navigation bar (`<button class="tab-button" data-tab-index="0">Tab Title</button>`).
   * Second loop generates the corresponding content panels (`<div class="tab-panel" data-tab-panel-index="0">Tab Content</div>`).
4. **Interactive JS Action**: Once the HTML loads in the browser, the clientlib JS binds a click listener to the tab buttons. Clicking a button reads `data-tab-index`, hides all other panels, and applies the `active` styling class only to the matching panel.

---

### B. Child Page List (`childpagelist Component`)
*   **Location**: [childpagelist Component](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/childpagelist%20Component)
*   **Sling Model**: [ChildPageListModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/ChildPageListModel.java)
*   **Properties Stored**:
    *   `parentPath` (Pathbrowser): Path to the parent page.
    *   `limit` (Integer): Maximum number of sub-pages.

#### Flow Diagram
![Child Page List Request Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/childpagelist_flow_1779873408700.png)

#### Detailed Flow Explanation:
1. **Configuration**: The author configures the parent AEM path (e.g. `/content/weekend/us/en`) and a limit of child links to display.
2. **PageManager API Query**: The Sling Model adapts `ResourceResolver` to AEM's `PageManager` class. It fetches the parent page resource.
3. **Traversing Children**: The Model calls `parentPage.listChildren()` to fetch the child sub-pages. It iterates through the iterator, fetching each child page's title, description, and path.
4. **List Truncation**: The iteration is capped by the JCR configured `limit` property.
5. **HTML Generation**: HTL receives the list of page items and compiles standard HTML anchor links (`<a href="/content/weekend/us/en/child.html">Child Title</a>`) server-side.

---

### C. Character Panel (`characterpanel`)
*   **Location**: [characterpanel](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/characterpanel)
*   **Sling Model**: [CharacterPanelModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/CharacterPanelModel.java)
*   **Properties Stored**:
    *   `characters` (Multifield child resource): Stores hero profiles (`characterName`, `realName`, `fileReference`).

#### Flow Diagram
![Character Panel Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/custom_components_flow_1780385105020.png)

#### Detailed Flow Explanation:
1. **JCR Data Node**: Character items are authored via multifield and saved into JCR sub-nodes.
2. **Direct Mapping**: The Model uses Sling annotations to read JCR properties and map details (Names, Identity, Images) to Java objects.
3. **Sightly compilation**: HTL reads the properties and generates card containers.
4. **CSS presentation**: Premium styling transforms hover scales, shadows, and spacing.

---

### D. Team Gallery (`teamgallery`)
*   **Location**: [teamgallery](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/teamgallery)
*   **Sling Model**: [TeamGalleryModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/TeamGalleryModel.java)
*   **Properties Stored**:
    *   `galleryTitle` and `members` multifield list.

#### Flow Diagram
![Team Gallery Memory Mapping Diagram](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/memory_flow_diagram_1780383467481.png)

#### Detailed Flow Explanation:
1. **JCR Node Storage**: Profiles are stored as JCR child nodes representing team members.
2. **Memory Mapping**: The Model maps the JCR properties into `List<MemberItem>` array elements in memory.
3. **Sightly compilation**: Sightly templates loop over the memory list, outputting individual cards.
4. **CSS styling**: Scoped styling classes structure grid rows and layout columns.

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

#### Flow Diagram
![Mock API Direct Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/mockapi_flow_diagram_1780381943043.png)

#### Detailed Flow Explanation:
1. **JCR Settings**: Author inputs apiEndpoint URL and Limit properties.
2. **Server GET Call**: The Sling Model executes `init()`, maps configuration values, and performs a direct server-to-server call to the Mock API.
3. **Jackson Parsing & Flattening**: The JSON array response is parsed, recursively flattened into a key-value properties list, and the title/subtitle are dynamically resolved.
4. **HTL Rendering**: HTML structure is rendered on AEM before sending the page.
5. **Browser Search**: Client JS filters cards dynamically in the browser.

---

### G. Mock API Servlet Proxy (`mockapiproxy`)
*   **Location**: [mockapiproxy](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapiproxy)
*   **Servlet**: [MockApiProxyServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiProxyServlet.java)
*   **Properties Stored**: `title` (String), `apiEndpoint` (String), `limit` (Integer).

#### Flow Diagram
![Mock API Servlet Proxy Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/servlet_flow_diagram_1779698537297.png)

#### Detailed Flow Explanation:
1. **Instant Page Shell**: AEM serves HTML page with loading indicators immediately.
2. **Browser AJAX Request**: JavaScript clientlib executes `fetch('/content/.../mockapiproxy.users.json')`.
3. **Servlet Interceptor**: The OSGi Servlet captures request, reads current JCR resource property mappings, and runs Java backend fetching logic.
4. **Response Delivery**: Servlet limits JSON array elements and streams JSON payload back.
5. **Browser Render**: JS flattens properties, maps title/subtitle, and replaces loading spinner with card grid.

---

### H. Mock API Loopback (`mockapinojs`)
*   **Location**: [mockapinojs](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapinojs)
*   **Servlet**: [MockApiNoJsServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiNoJsServlet.java)
*   **Sling Model**: [MockApiNoJsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiNoJsModel.java)
*   **Properties Stored**: `title` (String), `apiEndpoint` (String), `limit` (Integer).

#### Flow Diagram
*(Uses the loopback request architecture detailed in Section 3)*

#### Detailed Flow Explanation:
1. **Model Binding**: Sling Model binds on page load.
2. **Header Copies**: Model retrieves incoming browser headers (Cookies/Authorization) and binds them.
3. **Loopback Fetch**: Model calls local servlet endpoint internally (`scheme://host:port + JCRPath + ".users.json"`).
4. **Servlet processing**: Servlet calls external Mock API, applies JCR limits, and responds.
5. **Sightly compilation**: Model flattens the loopback JSON and HTL renders cards server-side.

---

## 3. Integration Patterns Request Flow Comparison

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
