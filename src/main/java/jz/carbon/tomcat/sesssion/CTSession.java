package jz.carbon.tomcat.sesssion;

import org.apache.catalina.Manager;
import org.apache.catalina.SessionListener;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2016/12/26.
 */
public class CTSession extends StandardSession {
    private static final Log log = LogFactory.getLog(CTSession.class);

    public CTSession(Manager manager) {
        super(manager);
    }

    public String getInfo() {
        return getClass().getSimpleName() + "/1.0";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
    }


    protected void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        readBaseProperties(stream);
        readAttributes(stream);
        if (listeners == null)
            listeners = new ArrayList<SessionListener>();

        if (notes == null)
            notes = new Hashtable<String, Object>();
    }

    private void readBaseProperties(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        // Deserialize the scalar instance variables (except Manager)
        authType = null;        // Transient only
        creationTime = ((Long) stream.readObject()).longValue();
        lastAccessedTime = ((Long) stream.readObject()).longValue();
        maxInactiveInterval = ((Integer) stream.readObject()).intValue();
        isNew = ((Boolean) stream.readObject()).booleanValue();
        isValid = ((Boolean) stream.readObject()).booleanValue();
        thisAccessedTime = ((Long) stream.readObject()).longValue();
        principal = null;        // Transient only
        id = (String) stream.readObject();
        if (log.isDebugEnabled())
            log.debug("readObject() loading session " + id);

    }

    private void readAttributes(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        // Deserialize the attribute count and attribute values
        if (attributes == null)
            attributes = new ConcurrentHashMap<String, Object>();
        int attributeCount = ((Integer) stream.readObject()).intValue();

        boolean isValidSave = isValid;
        isValid = true;
        for (int i = 0; i < attributeCount; i++) {
            boolean loadFail = false;
            Object value;
            String name = null;
            String className = null;
            try {
                name = (String) stream.readObject();
                className = (String) stream.readObject();
                value = stream.readObject();
            } catch (Exception e) {
                log.warn(" Loading attribute fail " + name + ":" + className, e);
                continue;
            }

            if (!(value instanceof byte[])) {
                log.warn("Expected byte[] but got " + value.getClass().getName() + " - " + name + ":" + className);
                continue;
            }
            Object restoreObj;
            try {
                restoreObj = bytesToObject((byte[]) value);
            } catch (Exception e) {
                log.warn("Unable deserialize - " + name + ":" + className, e);
                continue;
            }
            if (restoreObj.getClass().getName().compareTo(className) != 0) {
                log.warn("Not match className " + restoreObj.getClass().getName() + " - " + name + ":" + className);
                continue;
            }
            if (exclude(name, restoreObj)) {
                continue;
            }

            attributes.put(name, restoreObj);
            if (log.isDebugEnabled())
                log.debug("  loading attribute '" + name + ":" + className + "' with value '" + restoreObj + "'");
        }
        isValid = isValidSave;
    }

    private void writeBaseProperties(ObjectOutputStream stream) throws IOException {
        // Write the scalar instance variables (except Manager)
        stream.writeObject(Long.valueOf(creationTime));
        stream.writeObject(Long.valueOf(lastAccessedTime));
        stream.writeObject(Integer.valueOf(maxInactiveInterval));
        stream.writeObject(Boolean.valueOf(isNew));
        stream.writeObject(Boolean.valueOf(isValid));
        stream.writeObject(Long.valueOf(thisAccessedTime));
        stream.writeObject(id);
        if (log.isDebugEnabled())
            log.debug("writeObject() storing session " + id);
    }

    private void writeAttributes(ObjectOutputStream stream) throws IOException {
        // Accumulate the names of serializable and non-serializable attributes
        String keys[] = keys();
        ArrayList<String> saveNames = new ArrayList<String>();
        ArrayList<Serializable> saveValues = new ArrayList<Serializable>();
        ArrayList<String> saveValuesClassName = new ArrayList<String>();
        // ArrayList<ImmutablePair<String, Object>> data = new ArrayList<ImmutablePair<String, Object>>();

        for (int i = 0; i < keys.length; i++) {
            Object value = attributes.get(keys[i]);
            if (value == null) {
                continue;
            }
            if (isAttributeDistributable(keys[i], value) && !exclude(keys[i], value)) {
                saveNames.add(keys[i]);
                saveValues.add((Serializable) value);
                saveValuesClassName.add(value.getClass().getName());
            } else {
                removeAttributeInternal(keys[i], true);
            }
        }
        // Serialize the attribute count and the Serializable attributes
        int attributeCount = saveNames.size();
        stream.writeObject(Integer.valueOf(attributeCount));

        for (int i = 0; i < attributeCount; i++) {
            try {
                if (log.isDebugEnabled())
                    log.debug("  storing attribute '" + saveNames.get(i) + ":" + saveValuesClassName.get(i) + "' with value '" + saveValues.get(i) + "'");
                byte[] bytes;
                try {
                    bytes = objectToBytes(saveValues.get(i));
                } catch (IOException eio) {
                    log.warn(sm.getString("standardSession.notSerializable", saveNames.get(i) + ":" + saveValuesClassName.get(i), id), eio);
                    continue;
                }
                stream.writeObject(saveNames.get(i));
                stream.writeObject(saveValuesClassName.get(i));
                stream.writeObject(bytes);
            } catch (NotSerializableException e) {
                log.warn(sm.getString("standardSession.notSerializable", saveNames.get(i) + ":" + saveValuesClassName.get(i), id), e);
                continue;
            }
        }
    }

    protected void writeObject(ObjectOutputStream stream) throws IOException {
        writeBaseProperties(stream);
        writeAttributes(stream);
    }


    protected ObjectInputStream getObjectInputStream(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);

        CustomObjectInputStream ois;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (manager instanceof CTSessionPersistentManager) {
            CTSessionPersistentManager ctSessionPersistentManager = (CTSessionPersistentManager) manager;
            ois = new CustomObjectInputStream(bis, classLoader, manager.getContainer().getLogger(),
                    ctSessionPersistentManager.getSessionAttributeValueClassNamePattern(),
                    ctSessionPersistentManager.getWarnOnSessionAttributeFilterFailure());
        } else {
            ois = new CustomObjectInputStream(bis, classLoader);
        }
        return ois;
    }

    protected Object bytesToObject(byte[] data) throws ClassNotFoundException, IOException {

        Object result;
        ObjectInputStream ois = null;
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream(data);
        ois = getObjectInputStream(bis);
        result = ois.readObject();
        ois.close();
        bis.close();
        return result;
    }

    protected byte[] objectToBytes(Object data) throws IOException {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        oos.writeObject(data);
        oos.close();
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }


    @Override
    public boolean isAttributeDistributable(String name, Object value) {
        return isSerializable(value);
    }

    private boolean isSerializable(Object obj) {
        if ((obj instanceof Object[])) {
            return isSeriralableArray((Object[]) obj);
        }
        if (!(obj instanceof Serializable)) {
            return false;
        } else if (obj instanceof Map) {
            stripMapNonSerializable((Map) obj);
        } else if (obj instanceof List) {
            stripCollectionNonSerializable((Collection) obj);
        }
        return true;
    }

    private boolean isSeriralableArray(Object[] objs) {
        if (!(objs instanceof Serializable[]))
            return false;
        for (Object obj : objs) {
            if (!isSerializable(obj))
                return false;
        }
        return true;
    }

    private void stripMapNonSerializable(Map map) {
        Object[] keys = map.keySet().toArray();
        for (Object key : keys) {
            Object obj = map.get(key);
            if (!isSerializable(key)) {
                if (log.isDebugEnabled())
                    log.debug("Remove from map " + key);
                map.remove(key);
            }
            if (!isSerializable(obj)) {
                if (log.isDebugEnabled())
                    log.debug("Remove from map " + key + " " + obj.getClass().getName());
                map.remove(key);
            }
        }
    }

    private void stripCollectionNonSerializable(Collection colection) {
        Object[] objs = colection.toArray();
        for (Object obj : objs) {
            if (!isSerializable(obj)) {
                if (log.isDebugEnabled())
                    log.debug("Remove from list " + obj);
                colection.remove(obj);
            }
        }
    }

}
