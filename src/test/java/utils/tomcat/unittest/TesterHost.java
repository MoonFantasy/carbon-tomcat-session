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
package utils.tomcat.unittest;

import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardPipeline;
import org.apache.juli.logging.Log;

import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class TesterHost implements Host {
    private Pipeline pipeline = null;


    public Log getLogger() {
        return null;
    }


    public ObjectName getObjectName() {
        return null;
    }


    public Pipeline getPipeline() {
        if (pipeline == null)
            pipeline = new StandardPipeline(this);
        return pipeline;
    }


    public Cluster getCluster() {
        return null;
    }


    public void setCluster(Cluster cluster) {
        // NO-OP
    }


    public int getBackgroundProcessorDelay() {
        return 0;
    }


    public void setBackgroundProcessorDelay(int delay) {
        // NO-OP
    }

    private String name = "TestHost";


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Container getParent() {
        return null;
    }


    public void setParent(Container container) {
        // NO-OP
    }


    public ClassLoader getParentClassLoader() {
        return null;
    }


    public void setParentClassLoader(ClassLoader parent) {
        // NO-OP
    }


    public Realm getRealm() {
        return null;
    }


    public void setRealm(Realm realm) {
        // NO-OP
    }


    public void backgroundProcess() {
        // NO-OP
    }


    public void addChild(Container child) {
        // NO-OP
    }


    public void addContainerListener(ContainerListener listener) {
        // NO-OP
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // NO-OP
    }


    public Container findChild(String name) {
        return null;
    }


    public Container[] findChildren() {
        return null;
    }


    public ContainerListener[] findContainerListeners() {
        return null;
    }


    public void removeChild(Container child) {
        // NO-OP
    }


    public void removeContainerListener(ContainerListener listener) {
        // NO-OP
    }


    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // NO-OP
    }


    public void fireContainerEvent(String type, Object data) {
        // NO-OP
    }


    public void logAccess(Request request, Response response, long time, boolean useDefault) {
        // NO-OP
    }


    public AccessLog getAccessLog() {
        return null;
    }


    public int getStartStopThreads() {
        return 0;
    }


    public void setStartStopThreads(int startStopThreads) {
        // NO-OP
    }


    public void addLifecycleListener(LifecycleListener listener) {
        // NO-OP
    }


    public LifecycleListener[] findLifecycleListeners() {
        return null;
    }


    public void removeLifecycleListener(LifecycleListener listener) {
        // NO-OP
    }


    public void init() throws LifecycleException {
        // NO-OP
    }


    public void start() throws LifecycleException {
        // NO-OP
    }


    public void stop() throws LifecycleException {
        // NO-OP
    }


    public void destroy() throws LifecycleException {
        // NO-OP
    }


    public LifecycleState getState() {
        return null;
    }


    public String getStateName() {
        return null;
    }


    public String getXmlBase() {
        return null;
    }


    public void setXmlBase(String xmlBase) {
        // NO-OP
    }


    public String getAppBase() {
        return null;
    }


    public void setAppBase(String appBase) {
        // NO-OP
    }


    public boolean getAutoDeploy() {
        return false;
    }


    public void setAutoDeploy(boolean autoDeploy) {
        // NO-OP
    }


    public String getConfigClass() {
        return null;
    }


    public void setConfigClass(String configClass) {
        // NO-OP
    }


    public boolean getDeployOnStartup() {
        return false;
    }


    public void setDeployOnStartup(boolean deployOnStartup) {
        // NO-OP
    }


    public String getDeployIgnore() {
        return null;
    }


    public Pattern getDeployIgnorePattern() {
        return null;
    }


    public void setDeployIgnore(String deployIgnore) {
        // NO-OP
    }


    public ExecutorService getStartStopExecutor() {
        return null;
    }


    public boolean getCreateDirs() {
        return false;
    }


    public void setCreateDirs(boolean createDirs) {
        // NO-OP
    }


    public boolean getUndeployOldVersions() {
        return false;
    }


    public void setUndeployOldVersions(boolean undeployOldVersions) {
        // NO-OP
    }


    public void addAlias(String alias) {
        // NO-OP
    }


    public String[] findAliases() {
        return null;
    }


    public void removeAlias(String alias) {
        // NO-OP
    }


    public String getInfo() {
        return null;
    }


    public Loader getLoader() {
        return null;
    }


    public void setLoader(Loader loader) {
        // NO-OP
    }


    public Manager getManager() {
        return null;
    }


    public void setManager(Manager manager) {
        // NO-OP
    }


    @SuppressWarnings("deprecation")
    public Object getMappingObject() {
        return null;
    }


    public DirContext getResources() {
        return null;
    }


    public void setResources(DirContext resources) {
        // NO-OP
    }


    @SuppressWarnings("deprecation")
    public void invoke(Request request, Response response) throws IOException, ServletException {
        // NO-OP
    }
}
