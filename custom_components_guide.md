# AEM Custom Components Architecture & Reference Guide

This guide provides a comprehensive technical overview of the custom AEM components developed in the **Weekend Project**. It explains how each component operates from dialog authoring to frontend rendering, and compares them with their corresponding **AEM Core Components**.

---

## 1. Custom Components Core Architecture

The diagram below illustrates the unified MVC-based execution flow shared by all custom components in this project:

![Custom Components Architecture Flow Diagram](file:///c:/Users/Project1/weekend/custom_components_flow.png)

### Core Lifecycle Steps:
1. **Configuration**: The author enters properties (e.g., multifield elements) in the dialog. AEM saves these under the JCR resource node.
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
    *   `tabs` (Multifield child resource): Stores individual list elements containing:
        *   `tabTitle` (String): Displayed on the clickable tab header.
        *   `tabContent` (String/RichText): Rendered inside the active tab panel.
*   **How it Works**:
    1. The HTL template loops over `model.tabs` twice: once to draw headers with `data-tab-index`, and once to draw contents.
    2. The JS script ([customtabs.js](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/customtabs/clientlibs/js/customtabs.js)) binds a click event listener to each header item.
    3. Clicking a header fetches its `data-tab-index`, removes the `active` class from all headers/panels, and applies it to the clicked header and matching panel.
*   **Related Core Component**: `core/wcm/components/tabs/v1/tabs`
*   **Core vs. Custom Comparison**:
    *   *Core Tabs*: Acts as a layout container. Authors drag-and-drop entirely separate components (e.g., Image, Title, Accordion) inside each tab.
    *   *Custom Tabs*: Strictly maps a text-based composite multifield list. Ideal when you only need structured text articles, avoiding nested component complexity.

---

### B. Child Page List (`childpagelist Component`)
*   **Location**: [childpagelist Component](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/childpagelist%20Component)
*   **Sling Model**: [ChildPageListModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/ChildPageListModel.java)
*   **Properties Stored**:
    *   `parentPath` (Pathbrowser String): Path to the parent page (e.g., `/content/weekend/us/en`).
    *   `limit` (Integer): Maximum number of child pages to query.
*   **How it Works**:
    1. The Sling Model adapts `ResourceResolver` to AEM's `PageManager` API.
    2. It retrieves the parent page via `pageManager.getPage(parentPath)`.
    3. It calls `parentPage.listChildren()` to traverse sub-pages.
    4. For each page up to the `limit`, it extracts Title, Navigation Title (or Name fallback), Description, and Path, storing them as a list of `PageItem` objects.
    5. HTL loops over the list to render links dynamically.
*   **Related Core Component**: `core/wcm/components/list/v2/list`
*   **Core vs. Custom Comparison**:
    *   *Core List*: Can build dynamic lists using child pages, JCR tags, search queries, or manually selected files, with support for pagination.
    *   *Custom ChildPageList*: Restricts authors to selecting a single parent path. It ensures fast repository lookups without risk of slow, unindexed query execution.

---

### C. Character Panel (`characterpanel`)
*   **Location**: [characterpanel](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/characterpanel)
*   **Sling Model**: [CharacterPanelModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/CharacterPanelModel.java)
*   **Properties Stored**:
    *   `characters` (Multifield child resource): Stores individual character blocks:
        *   `characterName` (String): Hero name.
        *   `realName` (String): Secret identity.
        *   `fileReference` (Pathbrowser String): DAM image asset path.
*   **How it Works**:
    1. The model retrieves the `./characters` node and maps the list of child resources directly.
    2. The HTL loops over the list, reading properties directly via `${item.properties.characterName}`.
    3. CSS styles these cards into a responsive flexbox directory layout with hover transitions.
*   **Related Core Component**: `core/wcm/components/teaser/v1/teaser`
*   **Core vs. Custom Comparison**:
    *   *Core Teaser*: Displays a single card representing an image, title, description, and link. Showing multiple profiles requires dragging multiple teaser components manually.
    *   *Custom CharacterPanel*: Consolidates all cards within a single JCR property and dialog multifield. It provides a consistent, self-contained roster page.

---

### D. Team Gallery (`teamgallery`)
*   **Location**: [teamgallery](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/teamgallery)
*   **Sling Model**: [TeamGalleryModel.java](file:///c:/Users/Project1/weekend/core/src/main/java/com/weekend/core/models/TeamGalleryModel.java)
*   **Properties Stored**:
    *   `galleryTitle` (String): Header text.
    *   `members` (Multifield child resource): Contains:
        *   `fullName` (String): Team member name.
        *   `role` (String): Corporate role.
        *   `imagePath` (Pathbrowser String): DAM profile photo.
*   **How it Works**:
    1. The model maps `./members` directly into a typed `List<MemberItem>` array in RAM.
    2. HTL loops through the members list to construct a visual profile grid of cards.
*   **Related Core Component**: No direct equivalent (authors combine separate **Image**, **Title**, and **Text** elements inside a **Layout Container**).
*   **Core vs. Custom Comparison**:
    *   *Core Layout*: Requires drag-and-drop of multiple child elements. Authors can easily break layouts or column widths.
    *   *Custom TeamGallery*: Standardizes member profile presentation. It guarantees that styles, sizing, and structure remain uniform.

---

### E. Card Component (`card`)
*   **Location**: [card](file:///c:/Users/Project1/weekend/ui.apps/src/main/content/jcr_root/apps/weekend/components/card)
*   **Properties Stored**:
    *   `title` (String)
    *   `textarea` (String)
*   **How it Works**:
    *   This is an **HTML-only component** (no Java backing class).
    *   HTL reads variables directly from the JCR properties map:
        ```html
        <h1>${properties.title}</h1>
        <h4>${properties.textarea}</h4>
        ```
*   **Related Core Component**: `core/wcm/components/text/v2/text` + `core/wcm/components/title/v2/title`
*   **Core vs. Custom Comparison**:
    *   *Core Text/Title*: Two separate components dragged and configured individually.
    *   *Custom Card*: Enforces structural pairing of a header and body text in a unified dialog.

---

## 3. Comparison matrix: Core Components vs. Custom Components

| Requirement / Use Case | Core Components | Custom Components |
| :--- | :--- | :--- |
| **Grid / Layout Structure** | Flexible, columns edited directly in browser. | Predefined, rigid, guaranteed layout styles. |
| **Content Nesting** | Allows nesting full components inside panels. | Restricts content to simple fields (text, paths). |
| **Authoring Speed** | Slower (requires dropping multiple components). | Faster (configured via a single dialog form). |
| **Complexity** | High (involves container policy configurations). | Low (self-contained logic). |
