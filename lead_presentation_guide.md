# Technical Presentation Guide: Child Page List & Custom Tabs Components

Intha guide unga Technical Lead keta kooda answers solra maari, deep architectural reasons, data type decisions, and workflow details-a (in Tanglish & English) explain pannum.

---

# Component 1: Child Page List Component

## 1. logical workflow Diagram
This flow explains how author configuration inside JCR becomes HTML elements on the web browser.

```
+------------------+          1. Reads config          +-----------------------+
|   JCR Node       | ================================> | ChildPageListModel    |
| /content/weekend |                                   | (Sling Model Java)    |
|  - parentPath    |          2. Adapts to             +-----------------------+
|  - limit         |          PageManager & Page                   ||
+------------------+                                               || 3. Resolves Page APIs
                                                                   || & applies filters (limit)
                                                                   \/
+------------------+          5. Loop list items       +-----------------------+
| Browser HTML     | <================================ | List<PageItem>        |
| (Page Cards CSS) |          (data-sly-list)          | (Java Data List)      |
+------------------+                                   +-----------------------+
```

---

## 2. Technical Q&A for your Lead (Child Page List)

### Q1: What did you do? (Enna pannon?)
* **Answer**: We built a dynamic component that queries and displays child pages under a selected parent path. Authors can set a limit on how many child pages to show. We included dynamic title fallbacks in Java to ensure a beautiful card view even if titles are missing.

### Q2: How did you do it? (Process flow)
1. **Component Registry**: Created `/apps/weekend/components/childpagelist Component/.content.xml` node.
2. **Dialog**: Added `/apps/weekend/components/childpagelist Component/_cq_dialog/.content.xml` containing:
   * A `pathfield` selector (`name="./parentPath"`) to browse paths.
   * A `numberfield` (`name="./limit"`) to filter result lists.
3. **Sling Model**: Built [ChildPageListModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/ChildPageListModel.java) which adapts JCR resource node properties, queries pages using the PageManager API, filters the loop with `limit`, and prepares a list of `PageItem` objects.
4. **HTL Rendering**: Used [childpagelist Component.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/childpagelist%20Component/childpagelist%20Component.html) to bind the Sling Model and loop over the results.

---

## 3. Data Types & Why We Used Them

| Property Name | JCR Node Property | Java Data Type | Why this Data Type? (Reasoning) |
| :--- | :--- | :--- | :--- |
| `parentPath` | `./parentPath` (String) | `String` | JCR paths are absolute strings (e.g. `/content/weekend/us/en`). |
| `limit` | `./limit` (Long) | `Integer` (**Wrapper Class**) | **CRITICAL LEAD QUESTION**: Why not primitive `int`? <br>If an author leaves the field blank, JCR does NOT create the property. If we use primitive `int`, it will resolve to `0` or throw injection errors. A limit of `0` means zero pages will render. Using wrapper class `Integer` allows the value to be `null` (unconstrained), rendering all child pages without limit restrictions. |
| `childPages` | *Internal List* | `List<PageItem>` | A list maintains sequential insertion order (so order of child pages in JCR is preserved in the UI) and integrates natively with HTL's `data-sly-list`. |
| `PageItem` | *Inner Class* | `static class` | Creates an encapsulated data structure carrying only the computed strings (`title`, `description`, `path`) to the HTL, avoiding passing the heavy JCR Page object to the frontend template. |

---

## 4. Line-by-Line Code & Config Details

### A. Sling Model Java: `ChildPageListModel.java`
* `@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)`
  * **`adaptables = Resource.class`**: This Model adapts directly from the JCR Resource (the component instance node).
  * **`DefaultInjectionStrategy.OPTIONAL`**: JCR properties are optional. If they don't exist yet (unconfigured component), the class will still instantiate without throwing an error (no NullPointerExceptions or initialization failures).
* `@ValueMapValue`
  * Injects JCR properties into class variables. It looks for a property in JCR matching the variable name (`parentPath` and `limit`).
* `@SlingObject private ResourceResolver resourceResolver;`
  * Safely injects the active request thread's `ResourceResolver`. It is used to adapt to `PageManager.class`.
* `@PostConstruct`
  * Executes the `init()` method automatically after all annotations are injected. This prevents running logic with null or uninitialized fields.
* `PageManager pageManager = resourceResolver.adaptTo(PageManager.class);`
  * Adapts the resource resolver to `PageManager`. **PageManager** is AEM's Core WCM API. It is safer and has higher performance than querying raw repository JCR nodes.
* `Iterator<Page> iterator = parentPage.listChildren();`
  * Returns child pages (excluding non-page nodes like assets or folders).
* **Fallback Title Logic**:
  ```java
  String title = child.getNavigationTitle();
  if (title == null) { title = child.getTitle(); }
  if (title == null) { title = child.getName(); }
  ```
  * Priority: **Navigation Title** (for SEO/Menu) -> **Page Title** (Main Page Heading) -> **Node Name** (URL name). This guarantees something is shown even if the author forgot to configure titles.

### B. HTL Sightlines: `childpagelist Component.html`
* `<sly data-sly-use.model="com.weekend.core.models.ChildPageListModel" />`
  * Binds the Sling Model class to HTL engine. We can access public getters using CamelCase (e.g. `model.childPages` calls `getChildPages()`).
* `data-sly-test="${model.parentPath}"`
  * Renders the grid container only if the parent path has been configured by the author.
* `data-sly-list.page="${model.childPages}"`
  * Loops through `model.childPages`. In each loop, the current item is accessible via the variable `page`.
* `href="${page.path}.html"`
  * JCR page paths do not have extensions. We append `.html` to make the links valid for web browsers.
* `class="cq-placeholder" data-emptytext="..."`
  * This element displays a grey banner in **AEM Edit Mode** if the component is empty, telling the author how to configure it.

---
---

# Component 2: Custom Tabs Component

## 1. logical workflow Diagram
This flow shows the multi-tab interface and client-side tab switching.

```
+--------------------+
|  Dialog Multifield | ====> Author adds list of items: Tab Title + Tab Content
|  name="./tabs"     |
+--------------------+
          || Saves as nested JCR sub-nodes: /tabs/item0, /tabs/item1...
          \/
+--------------------+
|  CustomTabsModel   | ====> Adapts Resource. Injects @ChildResource private List<TabItem> tabs
| (Java Sling Model) |
+--------------------+
          || Binds properties
          \/
+--------------------+
|  customtabs.html   | ====> Renders dual structure:
|  (HTL Template)    |       - Tab Headers List (<ul class="custom-tabs-headers">)
+--------------------+       - Tab Content Panels List (<div class="custom-tabs-panels">)
          || Active classes toggled via JS Click Event
          \/
+--------------------+
|   Browser View &   | ====> JS listens to click -> matches "data-tab-index"
|   Clientlibs JS    |       -> adds/removes CSS ".active" (display: block/none)
+--------------------+
```

---

## 2. Technical Q&A for your Lead (Custom Tabs)

### Q1: What did you do? (Enna pannon?)
* **Answer**: We built an interactive tabs component. Authors can add an unlimited number of tabs dynamically using a JCR **composite multifield**. First tab is set as active by default using HTL index properties. Tab-switching is handled client-side using a scoped JavaScript Clientlib.

### Q2: How did you do it? (Process flow)
1. **JCR Node**: Registered `/apps/weekend/components/customtabs/.content.xml`.
2. **Dialog Config**: Created a composite multifield component inside `_cq_dialog/.content.xml` with name `./tabs`. It holds nested fields `tabTitle` and `tabContent`.
3. **Sling Model**: Built [CustomTabsModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/CustomTabsModel.java) containing a nested model class `TabItem`. It uses `@ChildResource(name = "tabs")` to automatically inject the list of children subnodes under `./tabs` as a list of `TabItem` models.
4. **Clientlibs**: Designed a ClientLibraryFolder `weekend.components.customtabs` with CSS and JavaScript to handle active status transitions.
5. **HTL Rendering**: Used [customtabs.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/customtabs/customtabs.html) to render the tab headers and content panels.

---

## 3. Data Types & Why We Used Them

| Property Name | JCR Node Property | Java Data Type | Why this Data Type? (Reasoning) |
| :--- | :--- | :--- | :--- |
| `tabs` | `./tabs` (nested nodes) | `List<TabItem>` | Composite multifield saves multiple rows as child nodes under parent resource folder `./tabs`. We inject these child resources as a Java `List` to hold order and allow iterating in HTL. |
| `TabItem` | *Nested node properties* | `static class` | Models each JCR tab subnode. It encapsulates fields `tabTitle` (String) and `tabContent` (String) representing the configuration for a single tab row. |

---

## 4. Line-by-Line Code & Config Details

### A. Dialog Composite Multifield
* `<tabsList composite="{Boolean}true" ...>`
  * **`composite="{Boolean}true"`**: Informs AEM that each multifield row contains multiple fields (`tabTitle` and `tabContent`). AEM will save these as individual child nodes (`item0`, `item1`, etc.) containing distinct properties, rather than saving them as a single String-array property.
* `<field name="./tabs" ...>`
  * The parent container name `./tabs` is where all row configurations are written in JCR.

### B. Sling Model Java: `CustomTabsModel.java`
* `@ChildResource(name = "tabs")`
  * Injects child resources located under JCR node `tabs` directly. Sling automatically maps each child resource (row) to a `TabItem` instance by matching property names.
* `@Model(adaptables = Resource.class)` inside `TabItem`
  * Allows the inner class itself to act as a Sling Model, resolving the fields `./tabTitle` and `./tabContent` automatically from child resources.

### C. HTL View & Sightly: `customtabs.html`
* `<sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html">`
  * Standard template used to fetch the client library utility wrapper.
* `<sly data-sly-call="${clientlib.all @ categories='weekend.components.customtabs'}" />`
  * Dynamically injects both JS and CSS script tags of category `weekend.components.customtabs` onto the page.
* `${tabList.first ? 'active' : ''}`
  * HTL exposes loop status variables helper `[varName]List`. For `data-sly-list.tab`, we get `tabList`. We check if it is the first item (`tabList.first`) and inject the CSS class `active` to ensure the first tab is selected by default when the page renders.
* `data-tab-index="${tabList.index}"`
  * Binds the 0-indexed loop counter as a custom DOM attribute. The Javascript clientlib uses this index to align the header to its correct panel.

### D. Clientlibs JavaScript: `customtabs.js`
* `(function () { ... })();`
  * An **IIFE** (Immediately Invoked Function Expression) to protect variables from polluting the global scope.
* `container.dataset.tabsInitialized`
  * A flag attribute added to the container to prevent adding multiple identical click listeners if the script runs multiple times in author mode.
* `window.jQuery(document).on("cq-content-loaded", function () { ... })`
  * **CRITICAL FOR AEM AUTHORS**: In AEM Author edit mode, when you save a dialog, AEM updates the page fragment asynchronously via AJAX without refreshing the entire browser window. The default `DOMContentLoaded` won't run again. Listening to `cq-content-loaded` ensures the tab interaction code re-initializes immediately after an author edits a component.

---
## 5. What happens if I change these configurations?

1. **Changing `name="./tabs"` in Dialog**:
   * If you rename it to `./myTabs`, the Sling Model will look for a child resource named `tabs` and return `null`. You must update `@ChildResource(name = "tabs")` in `CustomTabsModel.java` to `@ChildResource(name = "myTabs")` to match.
2. **Changing clientlib `categories` in `.content.xml`**:
   * If you change it from `[weekend.components.customtabs]` to `[my-custom-tabs]`, the component will fail to load styling and interactivity because the HTL calls the category `weekend.components.customtabs`. The HTL `categories` attribute must match the clientlib's category string.
3. **Changing `allowProxy="{Boolean}true"` in Clientlib**:
   * If you disable or remove this property, browser requests to `/etc.clientlibs/` paths won't be able to resolve and load the JS/CSS assets from the repository, resulting in 404 resource errors on the page.

---
---

# Component 3: FAQ Component with Expand/Collapse

## 1. logical workflow Diagram
This flow outlines how the collapsible FAQ accordion with bulk actions operates.

```
+--------------------+
|  Dialog Multifield | ====> Author adds list of items: Question + Answer
|  name="./faqList"  |
+--------------------+
          || Saves as nested JCR sub-nodes: /faqList/item0, /faqList/item1...
          \/
+--------------------+
|  FaqModel          | ====> Adapts Resource. Injects @ChildResource private List<FaqItem> faqList
| (Java Sling Model) |
+--------------------+
          || Binds properties
          \/
+--------------------+
|  faq.html          | ====> Renders questions list:
|  (HTL Template)    |       - Accordions (<div class="faq-item"> with question + panel)
+--------------------+
          || Active classes toggled via JS Click/Keypress Events
          \/
+--------------------+
|   Browser View &   | ====> - Click on a question closes other open items and toggles
|   Clientlibs JS    |         the active class ".active" on the clicked item.
+--------------------+
```

---

## 2. Technical Q&A for your Lead (FAQ Component)

### Q1: What did you do? (Enna pannon?)
* **Answer**: We built an FAQ section component with a centered "FAQ" header. Questions can be clicked to toggle answers with a sliding transition. We implemented a single-toggle accordion logic, meaning clicking a question opens its specific answer and automatically collapses any other open answers.

### Q2: How did you do it? (Process flow)
1. **JCR Node**: Registered `/apps/weekend/components/faq/.content.xml`.
2. **Dialog Config**: Created a composite multifield inside `_cq_dialog/.content.xml` named `./faqList` containing `./question` (textfield) and `./answer` (textarea).
3. **Sling Model**: Built [FaqModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/FaqModel.java) with nested static class `FaqItem`. It adapts the component resource and uses `@ChildResource(name = "faqList")` to inject list rows.
4. **Clientlibs**: Created client library `weekend.components.faq` with JS and CSS to support accordion behaviors, accessibility keys, and height transition effects.
5. **HTL Rendering**: Wrote [faq.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/faq/faq.html) to render the centered title, iterate over FAQ items, and bind properties.

---

## 3. Data Types & Why We Used Them

| Property Name | JCR Node Property | Java Data Type | Why this Data Type? (Reasoning) |
| :--- | :--- | :--- | :--- |
| `faqList` | `./faqList` (nested nodes) | `List<FaqItem>` | Captures rows of multifield items. We use a Java `List` to hold order and allow iterating in HTL. |
| `FaqItem` | *Nested node properties* | `static class` | Models each JCR faq subnode. It encapsulates fields `question` (String) and `answer` (String) representing a single question/answer row. |

---

## 4. Line-by-Line Code & Config Details

### A. Dialog Composite Multifield
* `<faqList composite="{Boolean}true" ...>`
  * **`composite="{Boolean}true"`**: Instructs AEM to save each row configuration under separate sub-nodes (e.g. `item0`, `item1`), mapping input keys into JCR properties on those nodes.
* `<field name="./faqList" ...>`
  * Sets the node folder path where row items are saved.

### B. Sling Model Java: `FaqModel.java`
* `@ChildResource(name = "faqList")`
  * Injects sub-nodes inside the `faqList` resource folder. Sling automatically maps properties (`question`, `answer`) on each child resource node to fields in `FaqItem`.

### C. Clientlibs CSS: `faq.css`
* `.faq-panel { max-height: 0; overflow: hidden; transition: max-height 0.3s cubic-bezier(0, 1, 0, 1); }`
  * Creates the collapsible panel body. Initial height is `0` and overflow is hidden to hide the answer content.
* `.faq-item.active .faq-panel { max-height: 1000px; transition: max-height 0.5s cubic-bezier(1, 0, 1, 0); }`
  * Animates height expansion to reveal the answers when the `active` class is added.
* `.faq-icon-indicator::before` / `::after` (CSS Plus/Minus animation)
  * Uses absolute positioning to form a vertical line and horizontal line (forming a `+` plus sign). When `.faq-item.active` is applied, the vertical line scales to `0`, transforming the indicator into a minus (`-`) sign with a smooth rotate transition.

### D. Clientlibs JavaScript: `faq.js`
* `faqList.addEventListener("click", function (e) { ... })`
  * **EVENT DELEGATION**: A single event listener is bound to the parent `.faq-list`. This reduces memory overhead, avoids multiple bindings in AEM edit mode, and correctly targets the active row via `e.target.closest(".faq-header")`.
* **SINGLE EXPANSION ACCORDION LOGIC**:
  * In the click handler, we retrieve all `.faq-item` nodes. We check if the clicked item is already active. If not, we remove `.active` from all other items to collapse them before adding `.active` to the clicked item.
* `e.stopPropagation()` and `e.preventDefault()`
  * **BUBBLING PREVENTION**: Stops the click event from bubbling up through parent nodes, ensuring clicks are isolated and don't trigger parent container handlers.
* Keyboard Accessibility (`keydown`)
  * **ACCESSIBILITY FOR LEADS**: Binds a delegated keyboard listener on the list. If screen-reader or keyboard-only users press `Enter` or `Spacebar` while focusing on the header, the row toggles active state, executing the same single-collapse logic.
* `container.dataset.faqInitialized = "true";`
  * Flag used to prevent binding click listeners multiple times if `initializeFaq()` is re-invoked during AEM authoring updates.

---
## 5. What happens if I change these configurations?

1. **Changing JCR Name to `./faqItems` in Dialog**:
   * JCR child resources will be stored under `/faqItems`. The Sling Model looking for `@ChildResource(name = "faqList")` will return `null`. You must update the `@ChildResource` annotation name in [FaqModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/FaqModel.java) to match.
2. **Disabling `allowProxy`**:
   * Clientlib will not be readable from `/etc.clientlibs/` proxy servlet path, leading to missing CSS and JS on publish/author pages.

---
---

# Component 4: API-Based Data Rendering Component (Mock Integration)

## 1. logical workflow Diagram
This diagram outlines the synchronous backend call and asynchronous search filtering.

```
+--------------------+
|  Dialog Config     | ====> Author specifies: apiEndpoint + Limit
| (JCR Properties)   |
+--------------------+
          || Binds properties
          \/
+--------------------+
|  MockApiModel      | ====> - Creates JDK 11 HttpClient
| (Java Sling Model) |       - Connects to REST API (e.g. JSONPlaceholder)
+--------------------+       - Jackson parses Array -> Maps items to List<MockUserItem>
          || Exposes items list to Sightly
          \/
+--------------------+
|  mockapi.html      | ====> - Binds Model
|  (HTL Template)    |       - Loops over user items list & renders card elements
+--------------------+       - Includes search box input bar
          || Filter updates via client-side input
          \/
+--------------------+
|  Browser View &    | ====> JS input listener filters cards by reading textContent
|  Clientlibs JS     |       matching queries. Displays "No results" panel if count is 0.
+--------------------+
```

---

## 2. Technical Q&A for your Lead (Mock API Component)

### Q1: What did you do? (Enna pannon?)
* **Answer**: We built an API integration component. It fetches mock user profiles server-side on page load, parses the array structure using Jackson, limits the output cards count, and displays them as grid panels. We also added a client-side search filter inside the clientlib.

### Q2: How did you do it? (Process flow)
1. **JCR Node**: Registered `/apps/weekend/components/mockapi/.content.xml`.
2. **Dialog Config**: Created a dialog configuration with textfield (`./apiEndpoint`) and numberfield (`./limit`).
3. **Sling Model**: Built [MockApiModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/MockApiModel.java). On `@PostConstruct` init, it triggers a GET request to the mock URL using JDK 11's HTTP client, parses user arrays with Jackson `ObjectMapper`, and creates a list of `MockUserItem` items.
4. **HTL Rendering**: Wrote [mockapi.html](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/mockapi/mockapi.html) to iterate and layout grid panels, binding the Sling Model.
5. **Clientlibs**: Created client library `weekend.components.mockapi` with CSS grid animations and JS input filter event listeners.

---

## 3. Data Types & Why We Used Them

| Property Name | JCR Node Property | Java Data Type | Why this Data Type? (Reasoning) |
| :--- | :--- | :--- | :--- |
| `apiEndpoint` | `./apiEndpoint` (String) | `String` | Endpoint URL string. Default fallback is hardcoded to `https://jsonplaceholder.typicode.com/users`. |
| `limit` | `./limit` (Long) | `Integer` | Wrapper class for the items display boundary, allowing default fallbacks to `6` if left unconfigured by authors. |
| `items` | *Internal List* | `List<MockUserItem>` | Holds parsed profile elements. Loops sequentially inside Sightly. |
| `MockUserItem` | *Inner Class* | `static class` | Capsule data holder for name, email, phone, website, company name, and first letter initials to represent user cards cleanly. |

---

## 4. Line-by-Line Code & Config Details

### A. Sling Model Java: `MockApiModel.java`
* `java.net.http.HttpClient`
  * **CRITICAL LEAD SELECTION**: Why use JDK 11's `HttpClient` instead of Apache `HttpClient` or Sling HTTP client? 
  * *Answer*: Built directly into JDK 11, requiring zero external OSGi bundle exports or package dependency declarations in `pom.xml`, avoiding potential version conflicts in OSGi bundles.
* `client.newBuilder().connectTimeout(Duration.ofSeconds(5))`
  * **TIMEOUT SECURITY**: Protects AEM thread pools. If the external REST server is down, request terminates in 5 seconds instead of hanging, which would block AEM thread pools and crash the author instance.
* `ObjectMapper mapper = new ObjectMapper(); JsonNode rootNode = mapper.readTree(response.body());`
  * Jackson parses the JSON body into JCR nodes map tree. We check if the root is an array before looping to prevent ClassCastException runtime failures.
* `public String getInitials()` inside inner class
  * HTL sightly expressions do not allow calling string functions with parameters (like `substring()`). We extract the first letter of the name on the Java side and expose it through this getter to avoid Sightly parser errors.

### B. Clientlibs JavaScript: `mockapi.js`
* `cards.forEach(function (card) { ... card.style.display = ""; ... })`
  * High-performance client-side search box filtering. Loops through user cards, checks if the search string is present in text content, and toggles display without triggering server roundtrips.

---
## 5. What happens if I change these configurations?

1. **Changing `apiEndpoint` JCR name**:
   * If you rename `./apiEndpoint` to `./url`, the value maps to `null` in the model, and the class will fall back to the default hardcoded JSONPlaceholder users endpoint.
2. **If Mock API returns a single object instead of an array**:
   * Jackson's `rootNode.isArray()` check will evaluate to `false`, the code sets an error message ("Mock API response is not a valid JSON Array"), and the HTL renders a clean error alert box instead of throwing exceptions.


