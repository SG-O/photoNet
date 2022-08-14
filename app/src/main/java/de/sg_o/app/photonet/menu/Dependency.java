/*
 *
 *   Copyright (C) 2022 Joerg Bayer (SG-O)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package de.sg_o.app.photonet.menu;

import android.content.res.XmlResourceParser;

public class Dependency {
    private final String name;
    private final String license;
    private final String web;

    @SuppressWarnings("unused")
    public Dependency(String name, String license, String web) {
        this.name = name;
        this.license = license;
        this.web = web;
    }

    public Dependency(XmlResourceParser xml) {
        String name = xml.getAttributeValue(null, "name");
        String license = xml.getAttributeValue(null, "license");
        String web = xml.getAttributeValue(null, "value");
        if (name == null) name = "";
        if (license == null) license = "";
        if (web == null) web = "";
        this.name = name;
        this.license = license;
        this.web = web;
    }

    public String getName() {
        return name;
    }

    public String getLicense() {
        return license;
    }

    public String getWeb() {
        return web;
    }
}
