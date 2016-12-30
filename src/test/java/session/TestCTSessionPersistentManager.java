package session;/*
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

import jz.carbon.tomcat.sesssion.CTSessionIdGenerator;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import mock.session.TesterStore;
import org.apache.catalina.Session;
import org.junit.Assert;
import org.junit.Test;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import utils.tomcat.unittest.TesterContext;
import utils.tomcat.unittest.TesterHost;
import org.apache.commons.codec.binary.Base64;


public class TestCTSessionPersistentManager {

    @Test
    public void testMinIdleSwap() throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(new TesterStore());

        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);

        manager.setContainer(context);

        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);

        manager.start();

        // Create the maximum number of sessions
        manager.createSession(null);
        manager.createSession(null);

        // Given the minIdleSwap settings, this should swap one out to get below
        // the limit
        manager.processPersistenceChecks();
        Assert.assertEquals(0,  manager.getActiveSessions());
        Assert.assertTrue(manager.getActiveSessionsFull() >= 2);

        manager.createSession(null);
        Assert.assertEquals(0,  manager.getActiveSessions());
        Assert.assertTrue(manager.getActiveSessionsFull() >= 3);
        manager.backgroundProcess();
    }

    @Test
    public void testMinIdleSwap2() throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1);
        manager.setStore(new TesterStore());

        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);

        manager.setContainer(context);

        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);


        manager.start();

        // Create the maximum number of sessions
        manager.createSession(null);
        manager.createSession(null);

        // Given the minIdleSwap settings, this should swap one out to get below
        // the limit
        manager.processPersistenceChecks();
        Assert.assertEquals(0,  manager.getActiveSessions());
        Assert.assertTrue(manager.getActiveSessionsFull() >= 2);

        manager.createSession(null);
        Assert.assertEquals(0,  manager.getActiveSessions());
        Assert.assertTrue(manager.getActiveSessionsFull() >= 3);
        manager.backgroundProcess();
    }

    @Test
    public void testSetSessionIdGeneratorClassName() throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);

        manager.setStore(new TesterStore());

        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);

        manager.setContainer(context);
        manager.setSessionIdGeneratorClassName(CTSessionIdGenerator.class.getName());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);

        manager.start();
        Session session = manager.createSession(null);
        Assert.assertTrue(Base64.isArrayByteBase64(session.getId().getBytes()));
        Assert.assertTrue(session.getId().length() <= manager.getSessionIdLength());

    }
}
