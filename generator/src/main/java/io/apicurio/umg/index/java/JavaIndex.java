/*
 * Copyright 2021 JBoss Inc
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

package io.apicurio.umg.index.java;

import java.util.HashMap;
import java.util.Map;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apicurio.umg.logging.Logger;
import lombok.Getter;

/**
 * @author eric.wittmann@gmail.com
 */
public class JavaIndex {

    @Getter
    private Map<String, JavaInterfaceSource> interfaces = new HashMap<>();

    @Getter
    private Map<String, JavaClassSource> classes = new HashMap<>();


    public JavaInterfaceSource lookupInterface(String fullyQualifiedName) {
        //Logger.debug("[JavaIndex] Looking up interface: " + fullyQualifiedName);
        return this.interfaces.get(fullyQualifiedName);
    }

    public JavaClassSource lookupClass(String fullyQualifiedName) {
        return this.classes.get(fullyQualifiedName);
    }

    public void index(JavaClassSource _class) {
        String fqcn = _class.getCanonicalName();
        this.classes.put(fqcn, _class);
    }

    public void index(JavaInterfaceSource _iface) {
        String fqcn = _iface.getCanonicalName();
        Logger.debug("[JavaIndex] Indexing interface: " + _iface.getQualifiedName());
        this.interfaces.put(fqcn, _iface);
    }

}
