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
package session;

import jz.carbon.tomcat.sesssion.CTSession;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestCTSession {

    private static final Manager TEST_MANAGER;

    static {
        TEST_MANAGER = new CTSessionPersistentManager();
        TEST_MANAGER.setContainer(new StandardContext());
    }

    @Test
    public void testGetInfo() throws Exception {
        CTSession session = new CTSession(TEST_MANAGER);
        assertEquals(CTSession.class.getSimpleName()+"/1.0", session.getInfo());
    }

    @Test
    public void testSerializationEmpty() throws Exception {

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        CTSession s2 = serializeThenDeserialize(s1);

        validateSame(s1, s2, 0);
    }


    @Test
    public void testSerializationSimple01() throws Exception {

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        s1.setAttribute("attr01", "value01");

        CTSession s2 = serializeThenDeserialize(s1);

        validateSame(s1, s2, 1);
    }


    @Test
    public void testSerializationSimple02() throws Exception {

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        s1.setAttribute("attr01", new NonSerializable());

        CTSession s2 = serializeThenDeserialize(s1);

        validateSame(s1, s2, 0);
    }


    @Test
    public void testSerializationSimple03() throws Exception {

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        s1.setAttribute("attr01", "value01");
        s1.setAttribute("attr02", new NonSerializable());

        CTSession s2 = serializeThenDeserialize(s1);

        validateSame(s1, s2, 1);
    }


    @Test
    public void testToString() {
        CTSession s1 = new CTSession(TEST_MANAGER);
        assertEquals(s1.getClass().getSimpleName() + "[" + s1.getId()+"]", s1.toString());
    }

    @Test
    public void serializeSkipsNonSerializableAttributes() throws Exception {
        final String nonSerializableKey = "nonSerializable";
        final String nestedNonSerializableKey = "nestedNonSerializable";
        final String serializableKey = "serializable";
        final String mapKey = "map";
        final String listKey = "list";
        final Object serializableValue = "foo";
        final String enumKey = "enum";

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        Map<String, NonSerializable> value = new HashMap<String, NonSerializable>();
        value.put("key", new NonSerializable());

        Map<Object, Object> map = new HashMap<Object, Object>();
        Map<Object, Object> submap = new HashMap<Object, Object>();
        Map<Object, Object> submap2 = new HashMap<Object, Object>();

        List<Object> list = new ArrayList<Object>();
        List<Object> sublist = new ArrayList<Object>();
        List<Object> sublist2 = new ArrayList<Object>();

        map.put(new NonSerializable(), "123");
        map.put(nonSerializableKey, new NonSerializable());
        map.put(serializableKey, "123");
        map.put(enumKey, TwoSide.LEFT);

        submap.put(new NonSerializable(), serializableValue);
        submap.put(nonSerializableKey, new NonSerializable());
        submap.put(serializableKey, serializableValue);
        submap.put(enumKey, TwoSide.LEFT);

        submap2.put(new NonSerializable(), serializableValue);
        submap2.put(nonSerializableKey, new NonSerializable());
        submap2.put(serializableKey, serializableValue);
        submap2.put(enumKey, TwoSide.RIGHT);

        map.put(mapKey, submap);


        list.add(new NonSerializable());
        list.add(serializableValue);
        list.add(new String[2]);

        sublist.add(new NonSerializable());
        sublist.add(serializableValue);
        sublist.add(new String[2]);
        sublist.add(submap2);

        map.put(listKey, sublist);

        sublist2.add(new NonSerializable());
        sublist2.add(serializableValue);
        sublist2.add(new String[2]);

        list.add(sublist2);


        s1.setAttribute(nestedNonSerializableKey, value);
        s1.setAttribute(serializableKey, serializableValue);
        s1.setAttribute(nonSerializableKey, new NonSerializable());

        s1.setAttribute(mapKey, map);
        s1.setAttribute(listKey, list);

        CTSession s2 = serializeThenDeserialize(s1);

//        assertNull(s2.getAttribute(nestedNonSerializableKey));
        assertEquals(0, ((Map)s2.getAttribute(nestedNonSerializableKey)).size());
        assertNull(s2.getAttribute(nonSerializableKey));
        assertEquals(serializableValue, s2.getAttribute(serializableKey));
    }


    private CTSession serializeThenDeserialize(CTSession source)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        source.writeObjectData(oos);

        CTSession dest = new CTSession(TEST_MANAGER);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        dest.readObjectData(ois);

        return dest;
    }


    private void validateSame(CTSession s1, CTSession s2, int expectedCount) {
        int count = 0;
        Enumeration<String> names = s1.getAttributeNames();
        while (names.hasMoreElements()) {
            count ++;
            String name = names.nextElement();
            Object v1 = s1.getAttribute(name);
            Object v2 = s2.getAttribute(name);

            assertEquals(v1,  v2);
        }

        assertEquals(expectedCount, count);
    }


    private enum TwoSide {
        RIGHT, LEFT
    }

    private static class NonSerializable {
    }
}
