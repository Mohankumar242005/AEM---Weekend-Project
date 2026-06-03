# AEM Component Development - Boilerplate & Configurations Guide

This guide details the specific codes, annotations, imports, XML nodes, methods, and configurations required to build functional Adobe Experience Manager (AEM) components. It provides a logical explanation of *why* each configuration is written.

---

## 1. Component Metadata Node (`.content.xml`)
**File Path:** `apps/<project>/components/<name>/.content.xml`  
**Purpose:** Registers the component with the JCR database, setting its display name and catalog group.

### The Code
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0"
    jcr:primaryType="cq:Component"
    jcr:title="Mock API Data"
    jcr:description="Component description..."
    componentGroup="Weekend Project - Content"/>
```

### Logical Explanation
*   `jcr:primaryType="cq:Component"`: Tells Sling to treat this folder structure as an authorable AEM Component.
*   `jcr:title`: The display name authors see in the AEM Sites component finder panel and dialogs.
*   `componentGroup`: The catalog folder in the AEM side panel where this component sits. Without this, authors cannot find or drag the component onto the page layout.

---

## 2. Edit Dialog Definition (`_cq_dialog/.content.xml`)
**File Path:** `apps/<project>/components/<name>/_cq_dialog/.content.xml`  
**Purpose:** Defines the author popup window where users enter custom parameters.

### The Code
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0" xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    jcr:primaryType="nt:unstructured"
    jcr:title="Component Configuration Settings"
    sling:resourceType="cq/gui/components/authoring/dialog">
    <content
        jcr:primaryType="nt:unstructured"
        sling:resourceType="granite/ui/components/coral/foundation/container">
        <items jcr:primaryType="nt:unstructured">
            <tabs
                jcr:primaryType="nt:unstructured"
                sling:resourceType="granite/ui/components/coral/foundation/tabs"
                maximized="{Boolean}true">
                <items jcr:primaryType="nt:unstructured">
                    <properties
                        jcr:primaryType="nt:unstructured"
                        jcr:title="Configuration"
                        sling:resourceType="granite/ui/components/coral/foundation/container"
                        margin="{Boolean}true">
                        <items jcr:primaryType="nt:unstructured">
                            <apiEndpoint
                                jcr:primaryType="nt:unstructured"
                                sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
                                fieldLabel="API Endpoint URL"
                                name="./apiEndpoint"/>
                        </items>
                    </properties>
                </items>
            </tabs>
        </items>
    </content>
</jcr:root>
```

### Logical Explanation
*   `sling:resourceType="cq/gui/components/authoring/dialog"`: Instantiates standard touch-UI dialog container templates from AEM Core libraries.
*   `sling:resourceType="granite/ui/components/.../form/textfield"`: Declares the input field type (e.g., standard text box, number selector, checkbox, path browser).
*   `name="./apiEndpoint"`: **Crucial mapping code.** The prefix `./` instructs AEM to save the authored value as a property named `apiEndpoint` directly on the component's JCR node inside `/content`.

---

## 3. HTL Render Template (`<name>.html`)
**File Path:** `apps/<project>/components/<name>/<name>.html`  
**Purpose:** Formats the final page markup on the server-side, combining Java properties with HTML.

### The Code
```html
<!-- 1. Bind Sling Model Java Class -->
<sly data-sly-use.model="com.weekend.core.models.MockApiModel" />

<!-- 2. Import & Execute Clientlib Loader -->
<sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html">
    <sly data-sly-call="${clientlib.all @ categories='weekend.components.mockapi'}" />
</sly>

<!-- 3. Renders Content Loop -->
<div class="mock-api-container" data-sly-test="${model.cards || model.errorMessage}">
    <div class="mock-api-error-alert" data-sly-test="${model.errorMessage}">
        <strong>Error:</strong> ${model.errorMessage}
    </div>
    
    <div class="mock-api-grid" data-sly-list.card="${model.cards}">
        <div class="mock-api-card">
            <h3>${card.title}</h3>
            <p>${card.subtitle}</p>
        </div>
    </div>
</div>

<!-- 4. Render Edit Placeholder -->
<div data-sly-test.isEmpty="${!model.cards && !model.errorMessage}" 
     class="cq-placeholder" 
     data-emptytext="Mock API Data Component (Click wrench icon to edit properties)">
</div>
```

### Logical Explanation
*   `data-sly-use.model`: Connects the HTML file to the Sling Model Java object instance.
*   `categories='weekend.components.mockapi'`: Tells AEM to search JCR for client libraries tagged with this specific category string and load its CSS/JS files.
*   `data-sly-test`: Renders the wrapped HTML tag **only if** the condition evaluates to true. Prevents rendering empty containers.
*   `data-sly-list.card="${model.cards}"`: Loops over the java object collection retrieved by the getter `getCards()`. For each iteration, `card` exposes Java properties like `card.title` or `card.subtitle`.
*   `class="cq-placeholder"`: Renders a gray clickable bar in AEM author edit mode if the component doesn't have any properties set yet, so authors don't lose track of it on a page.

---

## 4. ClientLibrary Metadata Folder (`clientlibs/`)
**File Paths:**
*   `apps/<project>/components/<name>/clientlibs/.content.xml`
*   `apps/<project>/components/<name>/clientlibs/css.txt`  
**Purpose:** Registers, proxies, and compiles resource files (CSS & JS) for deployment.

### `.content.xml` Code
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0"
    jcr:primaryType="cq:ClientLibraryFolder"
    categories="[weekend.components.mockapi]"
    allowProxy="{Boolean}true"/>
```

### `css.txt` Code
```text
css/mockapi.css
```

### Logical Explanation
*   `jcr:primaryType="cq:ClientLibraryFolder"`: Instructs AEM to compile child files inside this folder using the Client Library compiler.
*   `categories="[weekend.components.mockapi]"`: Categorization name used to invoke/load this CSS/JS bundle in HTL scripts.
*   `allowProxy="{Boolean}true"`: Security configuration. Instructs AEM to proxy requests pointing to `/apps` through `/etc.clientlibs/` URLs. This prevents end-users from discovering directory paths inside the secure `/apps` folder.
*   `css.txt`: List of actual CSS file paths in order of inclusion to merge into a single compressed package file.

---

## 5. Sling Model Class (`Model.java`)
**File Path:** `core/src/main/java/com/<project>/core/models/<Name>Model.java`  
**Purpose:** Acts as the backend controller for HTL, querying values, handling APIs, and preparing objects.

### The Code
```java
package com.weekend.core.models;

// Import annotations
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MockApiModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String apiEndpoint;

    @ValueMapValue
    private Integer limit;

    private List<String> cards = new ArrayList<>();

    @PostConstruct
    protected void init() {
        // Fallback setting logic
        if (apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            apiEndpoint = "https://jsonplaceholder.typicode.com/users";
        }
        if (limit == null || limit <= 0) {
            limit = 6;
        }
    }

    public List<String> getCards() {
        return cards;
    }
}
```

### Logical Explanation
*   `@Model(adaptables = ...)`: Registers this class as a Sling Model that can bind from incoming browser requests or straight JCR resources.
*   `DefaultInjectionStrategy.OPTIONAL`: Prevents AEM from crashing or failing model injection if JCR properties are empty/not authored yet.
*   `@Self`: Injects the current calling HTTP servlet request. Useful for retrieving headers or cookies.
*   `@ValueMapValue`: Injects properties from the JCR node directly into the field, mapping names automatically (e.g. property `limit` maps to field `limit`).
*   `@PostConstruct`: Defines the method (`init()`) that runs automatically after all Sling mapping injections are complete. This is where API calls, logic, or variable fallbacks are initialized.

---

## 6. Sling Servlet Class (`Servlet.java`)
**File Path:** `core/src/main/java/com/<project>/core/servlets/<Name>Servlet.java`  
**Purpose:** Exposes web request routing selectors or paths, returning data (like JSON or XML) back to client-side requests.

### The Code
```java
package com.weekend.core.servlets;

// Import Servlet and Sling classes
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = { Servlet.class }, property = {
        "sling.servlet.resourceTypes=weekend/components/mockapi",
        "sling.servlet.selectors=users",
        "sling.servlet.extensions=json",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
@ServiceDescription("Mock API Servlet Component Router")
public class MockApiServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        // 1. Set return headers
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 2. Fetch context resource node
        ValueMap properties = req.getResource().getValueMap();
        String apiEndpoint = properties.get("apiEndpoint", String.class);

        // 3. Write data stream back to client
        resp.getWriter().write("{\"status\": \"OK\", \"endpoint\": \"" + apiEndpoint + "\"}");
    }
}
```

### Logical Explanation
*   `@Component(service = { Servlet.class }, property = { ... })`: Registers the Java class as an OSGi Component offering servlet capabilities to AEM request resolvers.
*   `sling.servlet.resourceTypes`: Binds servlet execution strictly to component JCR resource configurations, providing permission checks.
*   `sling.servlet.selectors`: Triggers the servlet only when the selector token matches (e.g. `path.users.json`).
*   `sling.servlet.extensions`: Restricts servlet outputs to specific format headers (e.g. `.json`).
*   `SlingSafeMethodsServlet`: Extends a servlet that safely implements read-only methods (`GET`). If writing is required (`POST`/`PUT`), `SlingAllMethodsServlet` must be used instead.
*   `resp.setContentType("application/json")`: Explicitly sets headers so client JS parser recognizes payload structure instantly without manual conversions.
