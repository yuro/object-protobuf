package pb;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by wallace on 14-1-15.
 */
public class PBMessage<T> {
    private List<PBInfo> m_fieldList;
    private T m_object;

    public PBMessage(T object) throws InvalidProtocolBufferException {

        init(object);
    }

    public PBMessage(Class<T> type) throws InvalidProtocolBufferException {
        init(type);
    }

    @SuppressWarnings("unchecked")
    private void init(Object object) throws InvalidProtocolBufferException {
        try {
            if (object instanceof Class<?>) {
                initByClassType((Class<?>) object);
            } else {
                m_fieldList = FieldBuilder.Build(object.getClass()).mInfo;
                m_object = (T) object;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw InvalidProtocolBufferException.invalidJavaType();
        }
    }

    public T getObject() {
        return m_object;
    }

    public int size() {
        int size = 0;
        try {
            for (PBInfo info : m_fieldList) {
                if (info.mLabel == PBWireFormat.FieldLabel.REPEATED) {
                    List<?> list = (List<?>) info.mField.get(m_object);
                    for (Object obj : list) {
                        if (info.isPolymMessage()) {
                            // 扩展多态功能
                            int polymTag = info.getPolymTag(obj.getClass());
                            size += PBCodedOutputStream.computeDynamicMessageSize(info.mTag, new PBMessage<Object>(obj), polymTag);
                        } else {
                            size += PBCodedOutputStream.computeFieldSize(info.mType, info.mTag, obj);
                        }
                    }
                } else {
                    size += PBCodedOutputStream.computeFieldSize(info.mType, info.mTag, info.mField.get(m_object));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public void serialize(PBCodedOutputStream output) {
        try {
            for (PBInfo info : m_fieldList) {
                if (info.mLabel == PBWireFormat.FieldLabel.REPEATED) {
                    List<?> list = (List<?>) info.mField.get(m_object);
                    for (Object obj : list) {
                        if (info.isPolymMessage()) {
                            // 扩展多态功能
                            int polymTag = info.getPolymTag(obj.getClass());
                            output.writeDynamicMessage(info.mTag, new PBMessage<Object>(obj), polymTag);
                        } else {
                            output.writeField(info.mType, info.mTag, obj);
                        }
                    }
                } else {
                    output.writeField(info.mType, info.mTag, info.mField.get(m_object));
                }
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(PBCodedInputStream input) {
        try {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) break;

                int fieldTag = PBWireFormat.getTagFieldNumber(tag);
                PBInfo field = getFieldByTag(fieldTag);
                parseAndMergeField(tag, field, input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PBBytes toByteString() {
        try {
            byte[] buf = new byte[this.size()];
            PBCodedOutputStream output = PBCodedOutputStream.newInstance(buf);
            this.serialize(output);
            return PBBytes.copyFrom(buf);
        } catch (Exception e) {
            e.printStackTrace();
            return PBBytes.EMPTY;
        }
    }

    @SuppressWarnings("unchecked")
    private void initByClassType(Class<?> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        FieldInfoCacheItem item = FieldBuilder.Build(type);
        m_fieldList = item.mInfo;
        m_object = (T) item.mConstructor.newInstance();
    }

    private PBInfo getFieldByTag(int tag) {
        for (PBInfo field : m_fieldList) {
            if (field.mTag == tag) {
                return field;
            }
        }
        return null;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private void parseAndMergeField(int tag, PBInfo pbInfo, PBCodedInputStream input) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (pbInfo == null || PBWireFormat.getTagWireType(tag) != pbInfo.mType.getWireType()) {
            input.skipField(tag);
        } else {
            if (pbInfo.mLabel == PBWireFormat.FieldLabel.REPEATED) {
                List list = (List) pbInfo.mField.get(m_object);
                if (pbInfo.mType == PBWireFormat.FieldType.MESSAGE) {
                    // 只有message并且在List里面才需要多态的支持
                    Class<?> cls = pbInfo.mClassType;
                    if (pbInfo.isPolymMessage()) {
                        int polymTag = (Integer) input.readField(PBWireFormat.FieldType.INT32);
                        cls = pbInfo.getPolymClass(polymTag);
                    }
                    PBMessage msg = new PBMessage(cls);
                    input.readMessage(msg);
                    list.add(msg.getObject());
                } else {
                    list.add(input.readField(pbInfo.mType));
                }
            } else {
                if (pbInfo.mType == PBWireFormat.FieldType.MESSAGE) {
                    PBMessage msg = new PBMessage(pbInfo.mClassType);
                    input.readMessage(msg);
                    pbInfo.mField.set(m_object, msg.getObject());
                } else {
                    pbInfo.mField.set(m_object, input.readField(pbInfo.mType));
                }
            }
        }
    }
}


