package com.jtprince.bingo.kplugin.automark

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.board.SetVariables
import java.io.IOException
import java.io.InputStream
import java.util.logging.Level


class ItemTriggerYaml private constructor(
    @JsonProperty("item_triggers") root: Map<String, MatchGroup>
) {
    private val itemTriggers: Map<String, MatchGroup> = root
    companion object {
        val defaultYaml: ItemTriggerYaml by lazy {
            fromFile(ItemTriggerYaml::class.java.getResourceAsStream("/item_triggers.yml"))
        }

        /**
         * Load a YAML specification from an InputStream.
         */
        fun fromFile(yamlFile: InputStream?): ItemTriggerYaml {
            val yaml = ObjectMapper(YAMLFactory())
            // Allow `name` field to be either a String or list of Strings
            yaml.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            return try {
                yaml.readValue(yamlFile, ItemTriggerYaml::class.java)
            } catch (e: IOException) {
                BingoPlugin.logger.log(Level.SEVERE, "Could not parse item triggers yaml", e)
                ItemTriggerYaml(emptyMap())  // Return an empty YAML
            }
        }
    }

    val allAutomatedGoals: Set<String>
        get() = itemTriggers.keys

    /**
     * Get the specifications for the Item Trigger that is configured for a given goal ID. If the
     * goal ID is not present in item_triggers.yml, returns null. The return value is an Item Match
     * Group that can be considered the Root Item Match Group for this goal ID.
     */
    operator fun get(goalId: String): MatchGroup? {
        return itemTriggers[goalId]
    }

    /**
     * An Item Match Group is a node in a tree of Item Match Group objects, with the root of this
     * tree belonging to a goal ID. This mechanism is described in detail in README.md.
     */
    class MatchGroup @JsonCreator constructor(
        @JsonProperty("name") names: List<String>?,
        @JsonProperty("unique") unique: String?,
        @JsonProperty("total") total: String?,
        @JsonProperty("groups") children: List<MatchGroup>?
    ) {
        private val names: List<Regex> = names?.map{ s -> Regex(s) } ?: emptyList()
        private val unique: Variable = Variable(unique, 1)
        private val total: Variable = Variable(total, 1)
        internal val children: List<MatchGroup> = children ?: emptyList()

        fun nameMatches(name: String): Boolean {
            return names.any { it.matches(name) }
        }

        fun unique(setVariables: SetVariables): Int {
            return unique.getValue(setVariables)
        }

        fun total(setVariables: SetVariables): Int {
            return total.getValue(setVariables)
        }
    }

    internal class Variable(yamlVarValue: String?, defaultConstant: Int) {
        private val name: String?
        private val constant: Int?

        init {
            if (yamlVarValue?.startsWith("$") == true) {
                name = yamlVarValue.substring(1)
                constant = null
            } else {
                name = null
                constant = yamlVarValue?.toIntOrNull() ?: defaultConstant
            }
        }

        fun getValue(setVariables: SetVariables): Int {
            return constant ?: setVariables.getOrElse(name!!) {
                throw RuntimeException("Unknown variable $name")
            }
        }
    }
}
