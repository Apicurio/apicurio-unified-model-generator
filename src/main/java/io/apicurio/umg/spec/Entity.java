/*
 * Copyright 2020 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.umg.spec;

import java.util.Map;

/**
 * @author eric.wittmann@gmail.com
 */
public class Entity {
    
    private String name;
    private boolean root;
    private boolean extensible;
    private boolean referenceable;
    private Map<String, EntityProperty> properties;
    
    /**
     * Constructor.
     */
    public Entity() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * @return the properties
     */
    public Map<String, EntityProperty> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, EntityProperty> properties) {
        this.properties = properties;
    }

    /**
     * @return the referenceable
     */
    public boolean isReferenceable() {
        return referenceable;
    }

    /**
     * @param referenceable the referenceable to set
     */
    public void setReferenceable(boolean referenceable) {
        this.referenceable = referenceable;
    }

    /**
     * @return the extensible
     */
    public boolean isExtensible() {
        return extensible;
    }

    /**
     * @param extensible the extensible to set
     */
    public void setExtensible(boolean extensible) {
        this.extensible = extensible;
    }

}
