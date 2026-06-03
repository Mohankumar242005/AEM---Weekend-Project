# AEM Sling Servlets - Logical Architecture & Flow Guide

Vanakkam! Intha guide-la namma project-la **Sling Servlets**-a eppadi write pannanum, athoda types enna, real-world scenario (in Tanglish), and detailed flow/UML diagrams-a clean-ah paapom.

---

## 1. What is a Servlet in AEM? (AEM-la Servlet-na enna?)

Normal Java Web application-la Servlet-ngurathu HTTP requests-a (GET, POST, etc.) handle panni response (JSON, HTML, Text) tharra backend Java file. 

But AEM-la namma normal Servlets-a direct-a use panna maatom. Namma **Apache Sling Servlets** use panrom. Ithu standard Java Servlets mela build aana oru special layer, ithu direct-a OSGi container and JCR Database kooda integrate aagi work aagum.

---

## 2. Real-World Scenario: The Restaurant Analogy (Tanglish Scenario)

Imagine oru periya multi-cuisine Restaurant (AEM Site) irukku. Anga client HTTP request anupurathai oru food order flow kooda compare pannipom:

```
                                 [ RESTAURANT (AEM) ]
                                          │
                  ┌───────────────────────┴───────────────────────┐
                  ▼                                               ▼
     [ RESOURCE-TYPE BASED ]                              [ PATH-BASED ]
 (Chinese Table order -> Waiter)                   (Direct Hotline Call to Kitchen)
```

### Scenario A: Resource-Type Based Servlet (Table-Specific Service)
* **Real-world concept**: Neenga restaurant-la **Chinese Section Table** (Resource Type) la ukkanthu "Chinese Soup" order pandringa. Appo automatic-ah antha specific section waiter (Servlet) vanthu order-a eduppar. Avarukku neenga entha table-la ukkandhrukkinga (JCR Node Path) nu clear-ah theriyum. Avar unga table properties-a (salt, tissue count) access panna mudiyum.
* **AEM Mapping**: Request oru specific JCR node path mela cellum (e.g., `/content/weekend/us/en/jcr:content/root/teamgallery.txt`). Sling resolver antha resource-oda `sling:resourceType`-a paathu, atharkku match aana Servlet class-a execute pannum.─
* **Security**: Romba safe. User-kku antha JCR node path read access irundhal mattume intha servlet run aagum.

### Scenario B: Path-Based Servlet (Direct Hotline Call)
* **Real-world concept**: Neenga restaurant-kula varala, table-layum ukkala. Direct-a phone eduthu kitchen hotline number 108-kku call panni "Home Delivery Ginger Tea" kekringa. Server direct-a kitchen query process panni delivery kudukum. Ungaluku table path ethuvum thevai illa.
* **AEM Mapping**: Direct-a oru standard path-kku request anupuvom (e.g., `/bin/hellojava`). Resource resolver node metadata ethuvum check pannaathu. Direct-a endpoint target panni logic execute aagum.
* **Usage**: External system integrations or dynamic utilities AJAX logic-kku use aagum.

---

## 3. Request Flow Diagram

Intha flow diagram request eppadi browser-la irunthu code level logic map aagi execute aaguthu nu kaatudhu:

![Servlet Request Flow](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/servlet_flow_diagram_1779698537297.png)

---

## 4. UML Class Diagram

Servlet hierarchy structure and annotation components diagram representation:

![Servlet UML Class Diagram](file:///C:/Users/MohankumarM/.gemini/antigravity-ide/brain/08fa5e38-7a24-469a-abe5-8486744808dd/servlet_uml_class_diagram_1779698556503.png)

---

## 5. How to Write a Servlet in this Project (Step-by-Step)

Enga project-la irukka real-world servlets code example-a vachi detailed explanation paapom:

### Approach A: Path-Based Servlet Code (Direct Endpoint)
E.g., **[hellojava.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/hellojava.java)**

```java
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/hellojava"
})
public class hellojava extends SlingSafeMethodsServlet {
    ...
}
```

* **`@Component(service = Servlet.class)`**: Tells OSGi container to register this class as a Service component of type `Servlet`.
* **`"sling.servlet.paths=/bin/hellojava"`**: Standard registration path. Any GET request to `http://localhost:4502/bin/hellojava` triggers this.
* **`SlingSafeMethodsServlet`**: Used because GET is a safe (read-only/idempotent) HTTP method. If you want to handle POST/Write calls, extend **`SlingAllMethodsServlet`**.

### Approach B: Resource-Type Based Servlet (Preferred AEM Standard)
E.g., **[SimpleServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/SimpleServlet.java)**

```java
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="weekend/components/page",
        methods=HttpConstants.METHOD_GET,
        extensions="txt")
public class SimpleServlet extends SlingSafeMethodsServlet {
    ...
}
```

* **`@SlingServletResourceTypes`**: Maps servlet to components instead of open URLs.
* **`resourceTypes="weekend/components/page"`**: If request hits a resource that is built using this page template component, this servlet is resolved.
* **`extensions="txt"`**: Request selector extensions matches. For example: `/content/weekend/us/en.txt` will trigger this code.

---

## 6. Key Differences summary

| Feature | Path-Based Servlet (`/bin/...`) | Resource-Type Based Servlet |
| :--- | :--- | :--- |
| **Mapping** | URL path directly matches. | Node's `sling:resourceType` matches. |
| **Security** | Harder to control via ACL. Open to all authorized endpoints. | Easy to control using JCR permissions. |
| **Context** | Doesn't have direct access to a JCR content resource unless passed as a query param. | `request.getResource()` directly fetches the target page/component node. |
| **Best Practice** | Avoid unless using for external system webhooks/utilities. | Highly recommended for page layouts, exports, and UI components. |

---

## 7. Concrete GET & POST Code Examples in this Project

We have created two new endpoints under the `core` project to demonstrate GET and POST methods separately:

### A. GET Servlet (Safe / Idempotent) - MemberReadServlet
This servlet extends **`SlingSafeMethodsServlet`**. It handles GET requests to fetch data without altering the state of AEM JCR:

* **File**: [MemberReadServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MemberReadServlet.java)
* **Endpoint**: `/bin/members/read`
* **Purpose**: Fetches a sample JSON output representation of a member list read query.

```java
@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/members/read",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class MemberReadServlet extends SlingSafeMethodsServlet {
    @Override
    protected void doGet(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\": \"active\", \"service\": \"GET\", \"message\": \"Team data read successfully.\"}");
    }
}
```

### B. POST Servlet (Unsafe / State-Altering) - MemberWriteServlet
This servlet extends **`SlingAllMethodsServlet`**. It handles POST requests to write, update, or process form inputs:

* **File**: [MemberWriteServlet.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/servlets/MemberWriteServlet.java)
* **Endpoint**: `/bin/members/write`
* **Purpose**: Processes a request containing `fullName` and `role` parameters and returns a JSON response indicating whether the save operation was successful.

```java
@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/members/write",
        "sling.servlet.methods=" + HttpConstants.METHOD_POST
})
public class MemberWriteServlet extends SlingAllMethodsServlet {
    @Override
    protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String fullName = req.getParameter("fullName");
        String role = req.getParameter("role");

        if (fullName == null || role == null) {
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing parameters\"}");
            return;
        }
        resp.getWriter().write("{\"status\": \"success\", \"received\": {\"fullName\": \"" + fullName + "\"}}");
    }
}
```

---

## 8. Testing AEM Servlets using Postman (Step-by-Step)

AEM local author instance-la servlets test panna **Postman**-la oru sila settings set pannanum (Authentication & CSRF handling):

### Step 1: GET Servlet Test (`/bin/members/read`)
1. Postman open panni URL add panna: `http://localhost:4502/bin/members/read`.
2. Method dropdown list-la **`GET`** select panna.
3. Go to **Authorization** tab:
   * **Type**: `Basic Auth`
   * **Username**: `admin`
   * **Password**: `admin` (or unga local password).
4. Click **Send**.
5. Response content-la read query output view aagum.

### Step 2: POST Servlet Test (`/bin/members/write`)

AEM-la standard security mechanisms (CSRF protection filter) enabled-a irukkuradhala, normal POST request send panna direct-a **`403 Forbidden`** error response code varum. 

Athai resolve panni POST servlet check panna pathigal:

#### Option A: CSRF Excluded Paths Settings config (Recommended for Local Dev)
1. Browser-la OSGi Web Console open panna: `http://localhost:4502/system/console/configMgr`
2. Search and open configuration for: **"Adobe Granite CSRF Filter"**
3. Under **Excluded Paths**, add path: `/bin/members/write`
4. Click **Save**.
5. Once saved, Postman configuration panna:
   * URL: `http://localhost:4502/bin/members/write`
   * Method: **`POST`**
   * Authorization: **Basic Auth** (`admin` / `admin`).
   * Body: Select **`x-www-form-urlencoded`** radio button:
     * Key: `fullName`, Value: `Dhanush`
     * Key: `role`, Value: `Lead Developer`
   * Click **Send**. JSON success response code dynamic view aagum.

#### Option B: Fetch Token Dynamically (AEM Cloud Standard)
1. Postman-la GET request endpoint trigger panna: `http://localhost:4502/libs/granite/csrf/token.json` (using Basic Auth).
2. JSON output structure context read panna token response varum: `{"token": "abcd1234..."}`. Copy this token.
3. In your POST request (`/bin/members/write`):
   * Go to **Headers** tab.
   * Add new header row:
     * Key: **`CSRF-Token`**
     * Value: `[your_copied_token]`
   * Trigger **Send**. Endpoint maps successfully.


