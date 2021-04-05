package com.jtprince.bingo.plugin.automarking.itemtrigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemTriggerYaml {
    public final Map<String, MatchGroup> itemTriggers;

    @JsonCreator
    public ItemTriggerYaml(@JsonProperty("item_triggers") Map<String, MatchGroup> itemTriggers) {
        this.itemTriggers = itemTriggers;
    }

    public static ItemTriggerYaml fromFile(InputStream yamlFile) throws IOException {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        // Allow `name` field to be either a String or list of Strings
        yaml.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return yaml.readValue(yamlFile, ItemTriggerYaml.class);
    }

    public @Nullable ItemTriggerYaml.MatchGroup get(String goalId) {
        return itemTriggers.get(goalId);
    }

    public static class MatchGroup {
        private final List<Pattern> names;
        public final int unique;
        public final int total;
        public final List<MatchGroup> children;

        @JsonCreator
        public MatchGroup(@JsonProperty("name") List<String> name,
                          @JsonProperty("unique") Integer unique,
                          @JsonProperty("total") Integer total,
                          @JsonProperty("groups") List<MatchGroup> groups) {
            if (name != null) {
                this.names = name.stream().map(Pattern::compile).collect(Collectors.toList());
            } else {
                this.names = Collections.emptyList();
            }
            this.unique = Objects.requireNonNullElse(unique, 1);
            this.total = Objects.requireNonNullElse(total, 1);
            this.children = Objects.requireNonNullElse(groups, Collections.emptyList());
        }

        public boolean nameMatches(String name) {
            return names.stream().anyMatch(p -> p.matcher(name).matches());
        }
    }
}
