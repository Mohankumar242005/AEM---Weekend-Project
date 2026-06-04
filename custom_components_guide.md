# AEM Custom Components Architecture & Reference Guide

This guide provides a comprehensive technical overview of the custom AEM components developed in the **Weekend Project**. It explains how each component operates from dialog authoring to JCR storage, Sling Model parsing, and HTL presentation using detailed step-by-step logic and sample JCR configurations.

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

#### Request and Interactive Flow Diagram
![Custom Tabs Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/custom_tabs_flow_1780287459986.png)

#### Detailed Flow Explanation (With 2 Sample Records):

##### 1. JCR Storage (XML Representation)
When the author enters 2 tab configurations in the dialog:
*   *Tab 1:* Title = **"Service"**, Content = **"We offer AEM development."**
*   *Tab 2:* Title = **"Pricing"**, Content = **"Contact us for a quote."**

AEM creates a nested child resource node structure under the component's root node in the JCR:
```xml
<customtabs
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/customtabs">
    <tabs jcr:primaryType="nt:unstructured">
        <item0
            jcr:primaryType="nt:unstructured"
            tabTitle="Service"
            tabContent="We offer AEM development."/>
        <item1
            jcr:primaryType="nt:unstructured"
            tabTitle="Pricing"
            tabContent="Contact us for a quote."/>
    </tabs>
</customtabs>
```

##### 2. Sling Model Fetch Mechanism
*   **How it fetches:** The Sling Model reads the **entire child folder** (`tabs`) at once into memory rather than property-by-property.
*   **Code Reference:** In `CustomTabsModel.java` (Line 18), the annotation `@ChildResource(name = "tabs")` resolves the folder. It then adapts the child nodes (`item0`, `item1`) into instances of the static inner class `TabItem` where individual properties are resolved:
    ```java
    @ChildResource(name = "tabs")
    private List<TabItem> tabs; // Fetches entire node list at once
    ```
*   **Inner Class Property Resolution:** Inside `TabItem` (Line 31-35), properties are resolved one-by-one from each child node:
    ```java
    @ValueMapValue
    private String tabTitle; // Resolves for item0 ("Service"), then item1 ("Pricing")
    @ValueMapValue
    private String tabContent; // Resolves for item0 ("We offer AEM..."), then item1 ("Contact us...")
    ```

##### 3. Data Format Exposed
The Sling Model exposes the data to HTL as a typed Java List: `List<TabItem>`. Each `TabItem` is a structured Java Object containing getters for `tabTitle` and `tabContent`.

##### 4. HTL Loop Processing
The HTL template in [customtabs.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/customtabs/customtabs.html) reads the list from the getter `getTabs()` and loops through it:
```html
<div class="tab-buttons" data-sly-list.item="${model.tabs}">
    <button class="tab-btn" data-tab-index="${itemList.index}">${item.tabTitle}</button>
</div>
```
*   **How it iterates:** The loop runs **element-by-element**. Inside the loop, it evaluates individual properties (`item.tabTitle`) sequentially for each tab item to generate the buttons, and repeats the loop to draw the content panels.

---

### B. Child Page List (`childpagelist Component`)
*   **Location**: [childpagelist Component](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/childpagelist%20Component)
*   **Sling Model**: [ChildPageListModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/ChildPageListModel.java)

#### Request Flow Diagram
![Child Page List Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/childpagelist_flow_1779873408700.png)

#### Detailed Flow Explanation (With 2 Sample Pages):

##### 1. JCR Storage (XML Representation)
The author configures the component dialog with:
*   `parentPath` = **"/content/weekend/us/en"**
*   `limit` = **2**

AEM stores these properties directly on the component's node:
```xml
<childpagelist
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/childpagelist"
    parentPath="/content/weekend/us/en"
    limit="{Long}2"/>
```
The referenced parent page has two child pages in the JCR tree structure:
*   `/content/weekend/us/en/about` (`jcr:title="About Us"`, `jcr:description="Learn about our team."`)
*   `/content/weekend/us/en/contact` (`jcr:title="Contact Us"`, `jcr:description="Get in touch."`)

##### 2. Sling Model Fetch Mechanism
*   **How it fetches:** The Sling Model uses annotations to bind the configuration properties, then uses the AEM `PageManager` Java API to dynamically query the repository child pages.
*   **Code Reference:** In `ChildPageListModel.java` (Line 20-24), `@ValueMapValue` binds the parameters:
    ```java
    @ValueMapValue
    private String parentPath; // Injects "/content/weekend/us/en"
    @ValueMapValue
    private Integer limit; // Injects 2
    ```
*   **API Traversal:** During `init()` (Line 38-44), it opens the parent page and traverses the children **one-by-one** using an iterator:
    ```java
    Page parentPage = pageManager.getPage(parentPath);
    Iterator<Page> iterator = parentPage.listChildren(); // Gets iterator
    ```
    The model loops over the iterator, fetches page titles/descriptions, constructs `PageItem` objects, and breaks the loop once `limit` (2) is reached.

##### 3. Data Format Exposed
The model exposes a list of custom Page items: `List<PageItem>`. Each `PageItem` contains string attributes: `title`, `description`, and `path`.

##### 4. HTL Loop Processing
HTL iterates over the list element-by-element to output the child page link directory:
```html
<ul data-sly-list.page="${model.childPages}">
    <li>
        <a href="${page.path}.html">${page.title}</a>
        <p>${page.description}</p>
    </li>
</ul>
```

---

### C. Team Gallery (`teamgallery`)
*   **Location**: [teamgallery](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/teamgallery)
*   **Sling Model**: [TeamGalleryModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/TeamGalleryModel.java)

#### Request and Memory Mapping Flow Diagram
![Team Gallery Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/memory_flow_diagram_1780383467481.png)

#### Detailed Flow Explanation (With 2 Sample Members):

##### 1. JCR Storage (XML Representation)
The author configures:
*   `galleryTitle` = **"Our Team"**
*   *Member 1:* Full Name = **"Alice Smith"**, Role = **"Tech Lead"**, Image = **"/content/dam/weekend/alice.jpg"**
*   *Member 2:* Full Name = **"Bob Jones"**, Role = **"Developer"**, Image = **"/content/dam/weekend/bob.jpg"**

This JCR structure is created:
```xml
<teamgallery
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/teamgallery"
    galleryTitle="Our Team">
    <members jcr:primaryType="nt:unstructured">
        <item0
            jcr:primaryType="nt:unstructured"
            fullName="Alice Smith"
            role="Tech Lead"
            imagePath="/content/dam/weekend/alice.jpg"/>
        <item1
            jcr:primaryType="nt:unstructured"
            fullName="Bob Jones"
            role="Developer"
            imagePath="/content/dam/weekend/bob.jpg"/>
    </members>
</teamgallery>
```

##### 2. Sling Model Fetch Mechanism
*   **How it fetches:** The `galleryTitle` property is fetched individually, while the `members` composite sub-nodes are mapped as a single Child Resource list containing individual elements.
*   **Code Reference:** In `TeamGalleryModel.java` (Line 15-20):
    ```java
    @ValueMapValue
    private String galleryTitle; // Fetches "Our Team"
    @ChildResource(name = "members")
    private List<MemberItem> members; // Fetches the entire folder at once
    ```
    For each member node, properties are mapped to `MemberItem` fields:
    ```java
    @ValueMapValue
    private String fullName; // "Alice Smith" / "Bob Jones"
    @ValueMapValue
    private String role; // "Tech Lead" / "Developer"
    ```

##### 3. Data Format Exposed
Exposes `String` (galleryTitle) and `List<MemberItem>` (members) where each `MemberItem` is a DTO holding member properties.

##### 4. HTL Loop Processing
HTL parses the Title first, then loops through the list element-by-element to output the card grid:
```html
<h2>${model.galleryTitle}</h2>
<div class="gallery-grid" data-sly-list.member="${model.members}">
    <div class="member-card">
        <img src="${member.imagePath}" alt="${member.fullName}"/>
        <h3>${member.fullName}</h3>
        <p>${member.role}</p>
    </div>
</div>
```

---

## 3. API Integration Patterns Request Flow Comparison

Below is the visual overview comparing the request flow and rendering cycles of the three integration patterns:

![AEM Integration Architecture Flows](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/api_flows_simplified_1780486859793.png)

---

### D. Mock API Direct (`mockapi`)
*   **Location**: [mockapi](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapi)
*   **Sling Model**: [MockApiModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiModel.java)

#### Flow Diagram
![Mock API Direct Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/mockapi_flow_diagram_1780381943043.png)

#### Detailed Flow Explanation (With 2 Sample Records):

##### 1. JCR Storage (XML Representation)
The author configures:
*   `apiEndpoint` = **"https://jsonplaceholder.typicode.com/users"**
*   `limit` = **2**

```xml
<mockapi
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/mockapi"
    apiEndpoint="https://jsonplaceholder.typicode.com/users"
    limit="{Long}2"/>
```

##### 2. Sling Model Fetch Mechanism
*   **How it fetches:**
    1. AEM parses JCR properties (`apiEndpoint` and `limit`) on page load.
    2. The Model executes `init()`, maps these values, and initiates a server-to-server HTTP request using Java `HttpClient`.
    3. The Model receives the raw API JSON array string:
       `[{"id": 1, "name": "Leanne Graham", "email": "Sincere@april.biz"}, {"id": 2, "name": "Ervin Howell", "email": "Shanna@melissa.tv"}]`
    4. Jackson `ObjectMapper` parses this JSON array. It iterates through the array elements, dynamically flattens all properties, and maps titles/subtitles heuristically.
*   **Code Reference:** In `MockApiModel.java` (Line 81-83):
      ```java
      List<DynamicProperty> properties = new ArrayList<>();
      flattenNode("", node, properties); // Flattens all nested objects recursively
      ```

##### 3. Data Format Exposed
Exposes `List<DynamicCard>` containing Java objects where each object holds a `title` (resolved dynamically as "Leanne Graham"), `subtitle` ("Sincere@april.biz"), and a list of flattened attributes: `List<DynamicProperty>`.

##### 4. HTL Loop Processing
HTL iterates over the cards, and inside each card, iterates over its list of dynamic properties:
```html
<div class="mock-api-grid" data-sly-list.card="${model.cards}">
    <div class="mock-api-card">
        <h3>${card.title}</h3>
        <div class="details" data-sly-list.prop="${card.properties}">
            <span>${prop.key}: ${prop.value}</span>
        </div>
    </div>
</div>
```

---

### E. Mock API Servlet Proxy (`mockapiproxy`)
*   **Location**: [mockapiproxy](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapiproxy)
*   **Servlet**: [MockApiProxyServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiProxyServlet.java)

#### Flow Diagram
![Mock API Servlet Proxy Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/servlet_flow_diagram_1779698537297.png)

#### Detailed Flow Explanation (With 2 Sample Records):

##### 1. JCR Storage (XML Representation)
The author configures:
*   `title` = **"Users Proxy"**
*   `apiEndpoint` = **"https://jsonplaceholder.typicode.com/users"**
*   `limit` = **2**

```xml
<mockapiproxy
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/mockapiproxy"
    title="Users Proxy"
    apiEndpoint="https://jsonplaceholder.typicode.com/users"
    limit="{Long}2"/>
```

##### 2. How it works (Differing from Sling Model)
*   **AEM Server:** Instantly outputs the page HTML shell containing a loading spinner. The JCR path of the component `/content/.../mockapiproxy` is written as a data attribute (`data-resource-path`).
*   **Browser (AJAX Request):** JavaScript fetches the Servlet:
    `fetch('/content/.../mockapiproxy.users.json')`.
*   **Servlet Execution:** The servlet interceptor triggers [MockApiProxyServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiProxyServlet.java).
    1. The servlet reads `apiEndpoint` and `limit` from the JCR node dynamically using the request context:
       `ValueMap properties = req.getResource().getValueMap();`
    2. The servlet makes an HTTP request to the external endpoint.
    3. The servlet parses the response in Java, truncates the array elements to the JCR `limit` (2), and writes the limited raw JSON string back to the browser:
       `[{"id":1,"name":"Leanne Graham","email":"Sincere@april.biz"},{"id":2,"name":"Ervin Howell","email":"Shanna@melissa.tv"}]`
*   **Browser (JS DOM Injection):** The clientlib JS receives the raw JSON array. It iterates through the array elements, flattens the properties dynamically in JavaScript, and constructs the HTML card markup in the browser, replacing the loading spinner. **HTL is bypassed completely during card rendering.**

---

### F. Mock API Loopback (`mockapinojs`)
*   **Location**: [mockapinojs](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapinojs)
*   **Servlet**: [MockApiNoJsServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiNoJsServlet.java)
*   **Sling Model**: [MockApiNoJsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiNoJsModel.java)

#### Detailed Flow Explanation (With 2 Sample Records):

##### 1. JCR Storage (XML Representation)
```xml
<mockapinojs
    jcr:primaryType="nt:unstructured"
    sling:resourceType="weekend/components/mockapinojs"
    title="No-JS Loopback"
    apiEndpoint="https://jsonplaceholder.typicode.com/users"
    limit="{Long}2"/>
```

##### 2. How it works
1.  **Page Request:** The user requests the page. AEM initializes [MockApiNoJsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiNoJsModel.java).
2.  **Loopback Request:** The model constructs the loopback URL pointing back to the servlet:
    `http://localhost:4502/content/.../mockapinojs.users.json`.
    It copies the request's authentication cookies and headers to execute a GET call back to the AEM engine internally.
3.  **Servlet Processing:** The request routes to [MockApiNoJsServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MockApiNoJsServlet.java). The servlet reads `apiEndpoint` and `limit` from JCR, calls the external API, truncates the array to 2 elements, and returns the JSON payload back to the Sling Model.
4.  **Model Processing:** The Sling Model receives the JSON string. Jackson parses it, flattens all properties recursively, and compiles a `List<DynamicCard>` in AEM memory.
5.  **HTL Rendering:** HTL receives the cards list from the model and loops through it element-by-element to output the HTML. The browser receives the finished HTML cards with zero client-side JS executing.

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
