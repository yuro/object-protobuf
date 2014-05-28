object-protobuf
===============

A protocol buffer lib for android. 

这是一个非常优秀的Protocol Buffer的实现，使用对象序列化的思想实现。底层代码移植了Google的Java版Protocol Buffer库，使得兼容
大部分Protocol的数据，但是去除了Group等不常用的数据定义，减少代码size和性能消耗。如下是使用例子，

```
@ClassDef
public class SelfComposePb {
    @FieldDef(tag = 1)
    public int mInt;
    
    @FieldDef(tag = 2)
    public SelfComposePb mSelf;
}
```

```
public class PBTest extends TestCase {
	public void testSelfCompose() throws Exception {
		SelfComposePb pb = new SelfComposePb();
		pb.mInt = 1;
		
		SelfComposePb pb2 = new SelfComposePb();
		pb2.mInt = 2;
		pb.mSelf = pb2;
		
		PBMessage<SelfComposePb> msg = new PBMessage<SelfComposePb>(pb);
		
		PBCodedInputStream input  = PBCodedInputStream.newInstance(msg.toByteString().toByteArray());
		PBMessage<SelfComposePb> msg2 = new PBMessage<SelfComposePb>(SelfComposePb.class);
		msg2.parse(input);
		SelfComposePb pb3 = msg2.getObject();
		
		Assert.assertEquals(1, pb3.mInt);
		Assert.assertEquals(2, pb3.mSelf.mInt);
	}
}
```

非常简单，非常高效。数据的定义与数据的序列化操作分开，使得能够对任意已存在对象，进行pb序列化。
