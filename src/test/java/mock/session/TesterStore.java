/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mock.session;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesterStore implements Store {

    private Manager manager;
    private Map<String, Session> sessions = new HashMap<String, Session>();
    private List<String> savedIds = new ArrayList<String>();

    List<String> getSavedIds() {
        return savedIds;
    }

    public Manager getManager() {
        return this.manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public int getSize() throws IOException {
        return savedIds.size();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public String[] keys() throws IOException {
        return new ArrayList<String>(sessions.keySet()).toArray(new String[]{});
    }

    public Session load(String id) throws ClassNotFoundException,
            IOException {
        return sessions.get(id);
    }

    public void remove(String id) throws IOException {
        sessions.remove(id);
    }

    public void clear() throws IOException {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    public void save(Session session) throws IOException {
        sessions.put(session.getId(), session);
        savedIds.add(session.getId());
    }

    public String getInfo() {
        return null;
    }
}

