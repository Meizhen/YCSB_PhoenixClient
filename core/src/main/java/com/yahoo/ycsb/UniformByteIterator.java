package com.yahoo.ycsb;

import com.yahoo.ycsb.generator.*;
import com.yahoo.ycsb.generator.UniformIntegerGenerator;

public class UniformByteIterator extends ByteIterator{
	byte[] buf=new byte[1];
	int len;
	int off;
	/*IntegerGenerator generator;
	public UniformByteIterator(String generatorType){
		if (generatorType.equals("uniform")){
			generator=new UniformIntegerGenerator(1,200);
		}
		if (generatorType.equals("zipfian")){
			generator=new ScrambledZipfianGenerator(1,200);
		}
		if (generatorType.equals("exponential")){
			generator=new ExponentialGenerator(95,200);
		}
		buf[0]=(byte)(generator.nextInt());
	}*/
	public UniformByteIterator(IntegerGenerator generator){
		buf[0]=(byte)(generator.nextInt());
	}
	public boolean hasNext(){
		return false;
	}
	public long bytesLeft(){
		return 0;
	}
	public byte nextByte(){
		return buf[0];
	}

}
