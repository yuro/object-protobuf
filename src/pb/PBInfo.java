package pb;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by wallace on 14-1-15.
 */


public class PBInfo {
    public int mTag;
    public PBWireFormat.FieldType mType;
    public PBWireFormat.FieldLabel mLabel;
    public Field mField;
    public Class<?> mClassType;
    public List<PolymItem> mPolymList;

    public PBInfo(int tag, PBWireFormat.FieldType type, PBWireFormat.FieldLabel label, Field field, Class<?> realType, List<PolymItem> polymList) {
        mTag = tag;
        mType = type;
        mLabel = label;
        mField = field;
        mClassType = realType;
        mPolymList = polymList;
    }

    public int getPolymTag(Class<?> cls) throws InvalidProtocolBufferException {
        if (mPolymList != null) {
            for (PolymItem item : mPolymList) {
                if (item.mClass == cls) return item.mTag;
            }
        }
        throw InvalidProtocolBufferException.invalidTag();
    }

    public Class<?> getPolymClass(int tag) throws InvalidProtocolBufferException {
        if (mPolymList != null) {
            for (PolymItem item : mPolymList) {
                if (item.mTag == tag) return item.mClass;
            }
        }
        throw InvalidProtocolBufferException.invalidTag();
    }

    public boolean isPolymMessage() {
        if (mPolymList != null && mPolymList.size() > 0 && mType == PBWireFormat.FieldType.MESSAGE)
            return true;
        return false;
    }
}