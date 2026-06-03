package com.weekend.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

/**
 * Sling Model for the FAQ component with Expand/Collapse.
 * Maps composite multifield rows under "./faqList" to FaqItem objects.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FaqModel {

    @ChildResource(name = "faqList")
    private List<FaqItem> faqList;

    public List<FaqItem> getFaqList() {
        return faqList;
    }

    /**
     * Nested static Sling Model mapping the properties of a single FAQ entry.
     */
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class FaqItem {

        @ValueMapValue
        private String question;

        @ValueMapValue
        private String answer;

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
