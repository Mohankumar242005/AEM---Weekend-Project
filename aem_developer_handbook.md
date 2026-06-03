# The Ultimate AEM Developer's Handbook (Tanglish Edition)

Welcome to AEM (Adobe Experience Manager)! Intha handbook oru new beginner-kku AEM architecture-la irundhu advance Sling Servlets, APIs, and debugging varaikkum step-by-step explain panra oru full guide. Let's start this journey like a story!

---

# Table of Contents
1. [Chapter 1: AEM Architecture](#chapter-1-aem-architecture)
2. [Chapter 2: AEM Installation & Startup](#chapter-2-aem-installation--startup)
3. [Chapter 3: AEM Consoles](#chapter-3-aem-consoles)
4. [Chapter 4: AEM Cloud Local Setup](#chapter-4-aem-cloud-local-setup)
5. [Chapter 5: Project Setup & Maven Structure](#chapter-5-project-setup--maven-structure)
6. [Chapter 6: AEM Components & Core Components](#chapter-6-aem-components--core-components)
7. [Chapter 7: Dialogs & Granite UI](#chapter-7-dialogs--granite-ui)
8. [Chapter 8: Editable Templates & Policies](#chapter-8-editable-templates--policies)
9. [Chapter 9: Client Libraries (Clientlibs)](#chapter-9-client-libraries-clientlibs)
10. [Chapter 10: HTL / Sightly](#chapter-10-htl--sightly)
11. [Chapter 11: Content Fragments (CF) vs Experience Fragments (XF)](#chapter-11-content-fragments-cf-vs-experience-fragments-xf)
12. [Chapter 12: Assets & Digital Asset Management (DAM)](#chapter-12-assets--digital-asset-management-dam)
13. [Chapter 13: OSGi Service Architecture](#chapter-13-osgi-service-architecture)
14. [Chapter 14: OSGi Services & Configs](#chapter-14-osgi-services--configs)
15. [Chapter 15: Sling Models](#chapter-15-sling-models)
16. [Chapter 16: Sling Servlets](#chapter-16-sling-servlets)
17. [Chapter 17: Sling & WCM API Hierarchy](#chapter-17-sling--wcm-api-hierarchy)
18. [Chapter 18: Troubleshooting & Verification Cheat Sheet](#chapter-18-troubleshooting--verification-cheat-sheet)

---

# Chapter 1: AEM Architecture

## The Story of the AEM Kingdom 🏰
Imagine AEM-a oru periya corporate franchise mathiri.
* **Author Instance (The HQ Office 🏢)**: Inga thaan content writers and designers ukkanthu pages and assets-a upload panni modify pannuvanga. Ithu external visitors-kku visible aagathu.
* **Publish Instance (The Franchise Stores 🏪)**: Inga content read-only state-la irukkum. Web visitors inga thaan pages and assets access pannuvanga.
* **Dispatcher (The Security Guard & Cache Master 💂‍♂️)**: High traffic handle panna static pages cached pages-a standard storage layer-la maintain panni database load-a reduce pannum.

AEM server-kula modular-a intha stack details connected-a irukku:

![AEM Core Architecture](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/aem_core_architecture_1779795041668.png)

### The Stack Layers:
1. **OSGi Container (Apache Felix)**: Core infrastructure. Bundle lifecycle, dependencies injection control engine.
2. **Sling Web Framework**: URL request parsing mapping engine. URL mapping directly relates to JCR folders!
3. **JCR Repository (Jackrabbit Oak)**: Data storage layer. AEM stores everything as nodes and property files.

---

# Chapter 2: AEM Installation & Startup

## How to Install AEM locally (Step-by-Step)
1. **Setup Folder**: Create folder (e.g. `C:\AEM\author`).
2. **Copy Files**: Quickstart jar file (`aem-sdk-quickstart-xxxx.jar` or `cq-quickstart-xxxx.jar`) & `license.txt` intha directory-kulla copy pannanum.
3. **Rename Jar (Port mapping)**:
   * Jar name: `aem-author-p4502.jar` (tells AEM to run as **author** on port **4502**).
   * Publish node run panna: rename jar to `aem-publish-p4503.jar`.
4. **Launch**: Double-click the jar OR command line launch:
   ```cmd
   java -XX:MaxMetaspaceSize=1024m -Xmx4096m -jar aem-author-p4502.jar
   ```

## How to Verify:
* Browser URL check: `http://localhost:4502/`.
* Login Screen display checking (`admin` / `admin`).

## How to Debug startup issues:
* **Hangs at startup**: `crx-quickstart/logs/stdout.log` and `error.log` tail panni check panna bundle compilation block display aagum.
* **Out of Memory error**: JVM arguments `-Xmx4096m` memory limit parameter sets high-a irukanum.

---

# Chapter 3: AEM Consoles

AEM-la configuration changes settings update-ku multiple consoles use aagum:

| Console Name | Path | What you do here |
| :--- | :--- | :--- |
| **AEM Sites** | `/sites.html/content` | Create pages, edit templates, publish content. |
| **AEM Assets (DAM)** | `/assets.html/content/dam` | Upload images, videos, PDFs. Metadata configuration. |
| **CRXDE Lite** | `/crx/de/index.jsp` | Visual database manager. Browse JCR nodes directly. |
| **OSGi Console** | `/system/console/configMgr` | Configure active OSGi services, bundle updates status checking. |

---

# Chapter 4: AEM Cloud Local Setup

AEM Cloud Service (AEMaaCS) projects local setup differences:

1. **Cloud SDK**: Standard quickstart jar is Cloud Service SDK. Runmode always defaults to dev local environment.
2. **Local Dispatcher Tools**: Docker base panni local dispatcher setup dynamic mapping verify panna command line script runs:
   ```bash
   ./bin/docker_run.sh src/conf localhost:4502 8080
   ```
3. **Verification**: Access Dispatcher on `http://localhost:8080`.

---

# Chapter 5: Project Setup & Maven Structure

AEM projects standard code structure **Maven Multi-module structure** mapping-a complete details context structure paapom:

```
[project-reactor]
 ├── core (Java classes, Sling Models, Servlets)
 ├── ui.apps (HTML components, Dialogs, Clientlibs, apps node JCR)
 ├── ui.content (Page nodes, Assets, initial JCR configurations)
 ├── ui.config (OSGi config files)
 └── all (Combines ui.apps + ui.content jar into a single zip for package manager)
```

## How to Deploy:
1. **Full deployment** (slow - compiles everything):
   ```bash
   mvn clean install -PautoInstallSinglePackage
   ```
2. **Only backend bundle deployment** (fast - core Java compilation):
   ```bash
   mvn clean install -PautoInstallBundle -pl core
   ```

---

# Chapter 6: AEM Components & Core Components

Components are the building blocks of AEM pages.

* **AEM Core Components**: Adobe pre-built standard components (Breadcrumbs, Image, Teaser, Accordion, etc.) ready-to-use.
* **Proxy Component Pattern**: Core component-a direct-a code override panna koodadhu. Apps project location `/apps/weekend/components` kulla component folder template set panni:
  `sling:resourceSuperType = core/wcm/components/teaser/v2/teaser` property-a configure pannanum.
* **Component Registry**: Node property must have `jcr:primaryType="cq:Component"`.

---

# Chapter 7: Dialogs & Granite UI

Authors component settings configure panna **Wrench icon (Edit)** click panna display aagura form setup target.

## Dialog configuration file:
Location: `_cq_dialog/.content.xml`
```xml
<galleryTitle
    jcr:primaryType="nt:unstructured"
    sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
    fieldLabel="Title"
    name="./galleryTitle"/>
```
* **`sling:resourceType`**: Coral UI elements layout templates reference.
* **`name="./galleryTitle"`**: Binds field directly to JCR node property on page.
* **Multifield Composite**: `composite="{Boolean}true"` setup nodes JCR `members/item0` patterns resolution.

---

# Chapter 8: Editable Templates & Policies

## The Separation of Concerns
Template editor separates what authors can configure vs what is static:

![Editable Templates Structure](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/editable_templates_arch_1779795089988.png)

1. **Structure Layer (Locked)**: Header, footer, layout containers defined here. Page editor-la page create pannadhuku aprom author ithai delete or edit panna mudiyathu.
2. **Initial Content (Editable)**: Pre-populated cards templates. Authors edit and delete settings config values directly.
3. **Policies**: Allowed component list setup container. Configuration panel determines which component group belongs to which template container.

---

# Chapter 9: Client Libraries (Clientlibs)

AEM pools CSS and JS files into unified aggregated packages called **Clientlibs**.

* **Categories property**: Unique identification name (e.g. `category="weekend.theme"`).
* **HTL loading**:
  ```html
  <sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html">
      <sly data-sly-call="${clientlib.all @ categories='weekend.theme'}"/>
  </sly>
  ```
* **Proxy path property**: `allowProxy="{Boolean}true"` allows clientlibs under `/apps` to be fetched through `/etc.clientlibs/` path by public users safely.

---

# Chapter 10: HTL / Sightly

HTL is the modern HTML templating language in AEM.

```html
<!-- Binds Sling Model Java Class -->
<sly data-sly-use.model="com.weekend.core.models.TeamGalleryModel" />

<!-- Conditional Test -->
<h2 data-sly-test="${model.galleryTitle}">${model.galleryTitle}</h2>

<!-- Iterating Lists -->
<div data-sly-list.item="${model.members}">
    <p>${item.fullName @ context='html'}</p>
</div>
```

* **XSS Protection**: `@ context='html'` escapes unsafe user inputs to prevent script injections.

---

# Chapter 11: Content Fragments (CF) vs Experience Fragments (XF)

Understanding the difference between raw structured data vs visual blocks:

| Feature | Content Fragment (CF) | Experience Fragment (XF) |
| :--- | :--- | :--- |
| **Nature** | Purely structured data (No styling, No layout). | Channel-independent presentation unit (CSS styled component layout). |
| **Storage** | `/content/dam` (Stored as Assets). | `/content/experience-fragments` (Stored as Pages). |
| **Output format** | JSON payload export standard. | HTML fragments export. |
| **Usage** | Headless CMS, product specs list. | Header, Footer, Hero banners shared across multiple sites. |

---

# Chapter 12: Assets & Digital Asset Management (DAM)

DAM stores images, PDFs, audios, and video structures.

* Location: `/content/dam`
* **Renditions**: Uploaded asset metadata auto processes and cuts multiple sizes (thumbnails, responsive widths) stored inside `jcr:content/renditions` JCR folders.
* **DAM Workflows**: Dynamic update processes can trigger when asset is added.

---

# Chapter 13: OSGi Service Architecture

OSGi makes AEM modular. Every backend class can behave as an OSGi Component Service.

* **Component Annotation**:
  ```java
  @Component(service = HelloService.class)
  public class HelloServiceImpl implements HelloService { ... }
  ```
* **Injection Reference**:
  ```java
  @Reference
  private HelloService helloService;
  ```
* **Lifecycle hooks**: `@Activate` and `@Deactivate` methods execute when bundle initializes or stops.

---

# Chapter 14: OSGi Services & Configs

To supply config parameters dynamically to services:

* Config format: `com.weekend.core.services.HelloServiceImpl.cfg.json` file inside `ui.config` module.
* Read configurations in Service using Interfaces and annotations:
  ```java
  @ObjectClassDefinition(name="My Configuration")
  public @interface Config {
      String getMessage() default "Hello Default";
  }
  ```

---

# Chapter 15: Sling Models

Sling Models act as adapters from JCR nodes resources to Java classes:

![AEM Component Render Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/aem_component_flow_1779795065318.png)

## Core Injectors:
* **`@ValueMapValue`**: Injects JCR properties.
* **`@ChildResource`**: Injects child JCR resources list.
* **`@OSGiService`**: Injects OSGi services inside Sling Models.

---

# Chapter 16: Sling Servlets

AEM backend API endpoints.

## Path-based vs Resource-Type based Servlet
```java
// Path-based Registration
@Component(service = Servlet.class, property = { "sling.servlet.paths=/bin/my-endpoint" })

// Resource-Type based Registration (Best Practice)
@SlingServletResourceTypes(
    resourceTypes = "weekend/components/teamgallery",
    methods = HttpConstants.METHOD_POST,
    selectors = "update",
    extensions = "json"
)
```

* **CSRF Token**: POST calls require `CSRF-Token` header values to verify authorization and prevent 403 Forbidden.

---

# Chapter 17: Sling & WCM API Hierarchy

Best practice Java code hierarchy guideline layers:

1. **AEM Core APIs (`Page`, `Asset`, `Tag`)**: Highly preferred product classes.
2. **Sling APIs (`Resource`, `ValueMap`)**: Preferred lightweight JCR mapping layers.
3. **JCR APIs (`Session`, `Node`, `Property`)**: Avoid. Low-level, complex, and prone to breaking during platform upgrades.

---

# Chapter 18: Troubleshooting & Verification Cheat Sheet

* **Servlet not resolving (404)**: Verify active state of OSGi bundle in `/system/console/bundles`. Check Sling Servlet Resolver UI console: `/system/console/servletresolver` to verify mapping path selectors extensions configuration.
* **Properties not saving (400 / 403)**: Check if CSRF Filter OSGi properties exclude path is mapped. Check body parser keys format (`x-www-form-urlencoded`).
* **Sling model returning null values**: Ensure class imports default injection strategy matches: `@Model(..., defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)`.
