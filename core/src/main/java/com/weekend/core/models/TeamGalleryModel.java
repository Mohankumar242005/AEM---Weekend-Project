package com.weekend.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;

import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TeamGalleryModel {

    // Simple field mapped directly
    @ValueMapValue
    private String galleryTitle;

    // Multifield node injected directly as a list of static inner class instances
    @ChildResource(name = "members")
    private List<MemberItem> members;

    public String getGalleryTitle() {
        return galleryTitle;
    }

    public List<MemberItem> getMembers() {
        return members;
    }

    // --- Static Inner Class mapping the structure of single member item ---
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class MemberItem {

        @ValueMapValue
        private String fullName;

        @ValueMapValue

        private String role;

        @ValueMapValue
        private String imagePath;

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return role;
        }

        public String getImagePath() {
            return imagePath;
        }
    }
}
