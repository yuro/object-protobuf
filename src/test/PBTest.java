package test;

import pb.PBCodedInputStream;
import pb.PBMessage;
import junit.framework.Assert;
import junit.framework.TestCase;

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
