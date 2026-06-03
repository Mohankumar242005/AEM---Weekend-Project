package com.weekend.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ChildPageListModel {

    @ValueMapValue
    private String parentPath;

    @ValueMapValue
    private Integer limit;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<PageItem> childPages = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (parentPath != null && !parentPath.trim().isEmpty() && resourceResolver != null) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            if (pageManager != null) {
                Page parentPage = pageManager.getPage(parentPath);
                if (parentPage != null) {
                    Iterator<Page> iterator = parentPage.listChildren();
                    int count = 0;
                    while (iterator.hasNext()) {
                        if (limit != null && count >= limit) {
                            break;
                        }
                        Page child = iterator.next();
                        
                        // Fallbacks for title and description
                        String title = child.getNavigationTitle();
                        if (title == null) {
                            title = child.getTitle();
                        }
                        if (title == null) {
                            title = child.getName();
                        }
                        
                        String description = child.getDescription();
                        if (description == null) {
                            description = "No description available.";
                        }

                        childPages.add(new PageItem(title, description, child.getPath()));
                        count++;
                    }
                }
            }
        }
    }

    public List<PageItem> getChildPages() {
        return childPages;
    }

    public String getParentPath() {
        return parentPath;
    }

    public Integer getLimit() {
        return limit;
    }

    /**
     * Static helper class to represent a single child page details
     */
    public static class PageItem {
        private final String title;
        private final String description;
        private final String path;

        public PageItem(String title, String description, String path) {
            this.title = title;
            this.description = description;
            this.path = path;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getPath() {
            return path;
        }
    }
}
