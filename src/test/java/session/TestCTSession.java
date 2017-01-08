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
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TestCTSession {

    private static final Manager TEST_MANAGER;

    static {
        TEST_MANAGER = new CTSessionPersistentManager();
        TEST_MANAGER.setContainer(new StandardContext());
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


    /*
     * See Bug 58284
     */
    @Test
    public void serializeSkipsNonSerializableAttributes() throws Exception {
        final String nonSerializableKey = "nonSerializable";
        final String nestedNonSerializableKey = "nestedNonSerializable";
        final String serializableKey = "serializable";
        final Object serializableValue = "foo";

        CTSession s1 = new CTSession(TEST_MANAGER);
        s1.setValid(true);
        Map<String, NonSerializable> value = new HashMap<String, NonSerializable>();
        value.put("key", new NonSerializable());
        s1.setAttribute(nestedNonSerializableKey, value);
        s1.setAttribute(serializableKey, serializableValue);
        s1.setAttribute(nonSerializableKey, new NonSerializable());

        CTSession s2 = serializeThenDeserialize(s1);

        Assert.assertNull(s2.getAttribute(nestedNonSerializableKey));
        Assert.assertNull(s2.getAttribute(nonSerializableKey));
        Assert.assertEquals(serializableValue, s2.getAttribute(serializableKey));
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

            Assert.assertEquals(v1,  v2);
        }

        Assert.assertEquals(expectedCount, count);
    }


    private static class NonSerializable {
    }
}
