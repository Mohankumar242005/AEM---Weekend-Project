 # AEM API Preference Hierarchy - Applied to Weekend Project

Vanakkam! Intha guide-la namma project (`weekend` code) kooda compare panni, AEM-oda core API preference hierarchy-a romba detailed-ah and logically (in Tanglish) paapom.

---

## 1. Quick Recap of the API Hierarchy
AEM-la code ezhudhumbodhu namma access panra APIs-a 4 layers-a divide pannirukanga:

```
┌──────────────────────────────────────────────┐
│  Layer 1: AEM APIs (Page, Asset, Tag)        │  ◄── Most Preferred (High Level)
├──────────────────────────────────────────────┤
│  Layer 2: Sling APIs (Resource, ValueMap)    │  ◄── Preferred Resource Layer
├──────────────────────────────────────────────┤
│  Layer 3: JCR APIs (Session, Node, Property)  │  ◄── Use ONLY when necessary (Low Level)
├──────────────────────────────────────────────┤
│  Layer 4: OSGi APIs (Component, Reference)   │  ◄── Infrastructure Layer
└──────────────────────────────────────────────┘
```

The gold rule: **Always use the highest layer available!**

---
j
## 2. Layer 1: AEM Core APIs (Most Preferred)
AEM product-specific concepts-a (Pages, Asset manager, Tags) direct-a Java-la handle panna intha APIs use aagum.

### Our Project Connection:
Imagine namma component page detail read pannanum.
* **Bad Practice (JCR Node)**: Raw node-a page property check panna low-level logic execute panna koodathu.
* **Best Practice (AEM API)**: Direct-a `Page` API class load panna podhum:

```java
// Page API-a direct-a adapt panni read panrom
Page currentPage = resource.adaptTo(Page.class);
String pageTitle = currentPage.getTitle();
```

If we need to check if the member's profile image is a valid DAM asset:
```java
// Resource-a direct-a AEM Asset class-a adapt panrom
Asset asset = resourceResolver.getResource(imagePath).adaptTo(Asset.class);
String mimeType = asset.getMimeType(); // Gets image extension automatically
```

---

## 3. Layer 2: Sling APIs (Preferred Resource Layer)
AEM-la content are represented as **Resources**. Data properties read panna `ValueMap` or Sling Models standard dynamic inject helpers use panrom.

### Our Project Connection (Sling Models & Servlets):
Namma ezhuthuna **`TeamGalleryModel.java`** and Servlets completely intha Layer 2 layer base panni thaan work aaguthu.

#### Example A: Sling Models (`TeamGalleryModel.java`)
Namma raw JCR session open panni folders parse pannaama, direct-a annotations code mapping use pannon:
```java
@Model(adaptables = Resource.class)
public class TeamGalleryModel {
    
    @ValueMapValue // Sling API: Automatically reads JCR property
    private String galleryTitle;

    @ChildResource(name = "members") // Sling API: Automatically maps child resources to inner model list
    private List<MemberItem> members;
}
```
Ithu code logic-a lightweight and clean status control dynamic mapping block-a vachukuthu.

#### Example B: Sling Servlet Resources (`TeamGalleryServlet.java`)
JCR Node lookup bypass panni, direct-a request context data extraction logic parameters handle panna Sling API parameters direct map:
```java
Resource componentResource = req.getResource(); // Get Sling Resource
ValueMap properties = componentResource.getValueMap(); // Sling ValueMap mapping
String title = properties.get("galleryTitle", String.class);
```

---

## 4. Layer 3: JCR APIs (Use Only When Necessary)
JCR standard is low-level access (folders, nodes, raw session transactions). 

### Our Project Connection:
Namma **`TeamGalleryWriteServlet.java`** servlet file-la database write check execute panna Layer 3 session modify check control properties update panna:

* **Why we avoided JCR Node writes?**:
  Traditional JCR Node save approach has complex code and lacks null safety:
  ```java
  // Avoid this boilerplate if possible:
  Node node = componentResource.adaptTo(Node.class);
  node.setProperty("galleryTitle", newTitle);
  node.getSession().save();
  ```
* **What we did instead (Sling API Wrapper)**:
  Sling mapping using `ModifiableValueMap` is much safer and automatically resolves session transaction locks:
  ```java
  ModifiableValueMap properties = componentResource.adaptTo(ModifiableValueMap.class);
  properties.put("galleryTitle", newTitle);
  resolver.commit(); // Easy commit
  ```

---

## 5. Layer 4: OSGi APIs (Infrastructure Layer)
Servlets dependency injection class files OSGi lifecycle state dynamic controls mapping layers handles.

### Our Project Connection:
Namma servlets file header structure checks:
```java
@Component(service = { Servlet.class })
public class TeamGalleryWriteServlet extends SlingAllMethodsServlet {
    ...
}
```
* **`@Component`**: Registers the Java class inside the OSGi registry container.
* **`@Reference`**: Injects OSGi services directly.

---

## Summary Strategy table for Developer

| Requirement | Preferred Layer API | Example in `weekend` project |
| :--- | :--- | :--- |
| Read Page details | AEM WCM APIs (`Page`, `PageManager`) | Adapt to Page object to get templates or parents. |
| Access DAM photos | AEM DAM APIs (`Asset`) | Adapt image resource path to `Asset` to read metadata. |
| Component properties mapping | Sling Models (`@ValueMapValue`, `@Model`) | Map dialog variables directly in `TeamGalleryModel.java`. |
| Servlet request JCR operations | Sling Resource APIs (`Resource`, `ValueMap`) | Fetch and write JCR data safely using Sling APIs in servlets. |
| OSGi Service injection | OSGi annotations (`@Component`, `@Reference`) | Register servlets as OSGi declarative service components. |
