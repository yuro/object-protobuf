package test;

import pb.ClassDef;
import pb.FieldDef;


@ClassDef
public class SelfComposePb {
    @FieldDef(tag = 1)
    public int mInt;
    
    @FieldDef(tag = 2)
    public SelfComposePb mSelf;
}
