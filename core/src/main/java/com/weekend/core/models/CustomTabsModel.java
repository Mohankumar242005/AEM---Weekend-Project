package com.weekend.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

/**
 * Sling Model for the Custom Tabs component.
 * Maps the child resources created by the composite multifield.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomTabsModel {

    @ChildResource(name = "tabs")
    private List<TabItem> tabs;

    public List<TabItem> getTabs() {
        return tabs;
    }

    /**
     * Static inner class mapping properties of a single tab item
     */
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class TabItem {

        @ValueMapValue
        private String tabTitle;

        @ValueMapValue
        private String tabContent;

        public String getTabTitle() {
            return tabTitle;
        }

        public String getTabContent() {
            return tabContent;
        }
    }
}
