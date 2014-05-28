package pb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class PolymItem {
    public Class<?> mClass;
    public int mTag;

    public PolymItem(Class<?> cls, int tag) {
        mClass = cls;
        mTag = tag;
    }
}

class FieldInfoCacheItem {
    public Class<?> mClass;
    public Constructor<?> mConstructor;
    public List<PBInfo> mInfo;

    public FieldInfoCacheItem(Class<?> type, List<PBInfo> infoList) throws NoSuchMethodException {
        mClass = type;
        mConstructor = mClass.getDeclaredConstructor();
        //private access
        mConstructor.setAccessible(true);
        mInfo = infoList;
    }
}

public class FieldBuilder {
    private static final int MAX_CACHE_SIZE = 20;
    private static Queue<FieldInfoCacheItem> m_infoCache = new LinkedList<FieldInfoCacheItem>();

    public static FieldInfoCacheItem Build(Class<?> type) throws NoSuchMethodException {
        FieldInfoCacheItem item = getCache(type);
        if (item == null) {
            List<PBInfo> list = new ArrayList<PBInfo>();
            item = new FieldInfoCacheItem(type, list);

            m_infoCache.offer(item);
            if (m_infoCache.size() > MAX_CACHE_SIZE) {
                m_infoCache.poll();
            }
            addField(type, list);

            // 递归父类
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                ClassDef annMessage = superclass.getAnnotation(ClassDef.class);
                if (annMessage != null) {
                    addField(superclass, list);
                }
            }
        }
        return item;
    }

    private static void addField(Class<?> type, List<PBInfo> list) {
        for (Field field : type.getDeclaredFields()) {
            //private access
            field.setAccessible(true);
            FieldDef annField = field.getAnnotation(FieldDef.class);
            // 必须是有效的标注
            if (annField != null) {
                try {
                    PolymDef annPolym = field.getAnnotation(PolymDef.class);
                    List<PolymItem> polymList = new ArrayList<PolymItem>();
                    if (annPolym != null) {
                        Class<?>[] classes = annPolym.classes();
                        int[] tags = annPolym.tags();
                        if (classes.length != 0 && classes.length == tags.length) {
                            for (int i = 0; i < classes.length; i++) {
                                polymList.add(new PolymItem(classes[i], tags[i]));
                            }
                        } else {
                            throw InvalidProtocolBufferException.invalidTag();
                        }
                    }

                    Class<?> classType = getRealClassType(field);
                    if (annField.type() == PBWireFormat.FieldType.UNKNOWN) {
                        list.add(new PBInfo(
                                getFieldTag(annField.tag(), list),
                                getFieldType(classType),
                                getFieldLabel(field),
                                field,
                                classType,
                                polymList));
                    } else {
                        // 指定需要序列化成什么类型
                        list.add(new PBInfo(
                                getFieldTag(annField.tag(), list),
                                annField.type(),
                                getFieldLabel(field),
                                field,
                                classType,
                                polymList));
                    }

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static FieldInfoCacheItem getCache(Class<?> type) {
        for (FieldInfoCacheItem item : m_infoCache) {
            if (item.mClass == type) return item;
        }
        return null;
    }

    private static int getFieldTag(int value, List<PBInfo> list) throws InvalidProtocolBufferException {
        if (value == -1) {
            throw InvalidProtocolBufferException.invalidTag();
        } else {
            for (PBInfo field : list) {
                if (field.mTag == value) {
                    throw InvalidProtocolBufferException.invalidTag();
                }
            }
            return value;
        }
    }

    private static Class<?> getRealClassType(Field field) throws InvalidProtocolBufferException {
        Class<?> type = field.getType();
        if (type == List.class) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (types.length != 1) {
                throw InvalidProtocolBufferException.invalidJavaType();
            }
            return (Class<?>) types[0];
        } else {
            return type;
        }
    }

    private static PBWireFormat.FieldType getFieldType(Class<?> type) throws InvalidProtocolBufferException {
        ClassDef annMessage = type.getAnnotation(ClassDef.class);
        if (annMessage == null) {
            if (type == Integer.TYPE) return PBWireFormat.FieldType.INT32;
            else if (type == Float.TYPE) return PBWireFormat.FieldType.FLOAT;
            else if (type == Double.TYPE) return PBWireFormat.FieldType.DOUBLE;
            else if (type == Long.TYPE) return PBWireFormat.FieldType.INT64;
            else if (type == String.class) return PBWireFormat.FieldType.STRING;
            else if (type == PBBytes.class) return PBWireFormat.FieldType.BYTES;
            else if (type == Boolean.TYPE) return PBWireFormat.FieldType.BOOL;
            else throw InvalidProtocolBufferException.invalidJavaType();
        } else {
            return PBWireFormat.FieldType.MESSAGE;
        }
    }

    private static PBWireFormat.FieldLabel getFieldLabel(Field field) {
        Class<?> type = field.getType();
        if (type == List.class) return PBWireFormat.FieldLabel.REPEATED;
        else return PBWireFormat.FieldLabel.OPTIONAL;
    }
}
