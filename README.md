# feeyo-bufferpool

A Java buffer pool implementation

## Usage

```
  <dependency>
	<groupId>com.github.variflight</groupId>
	<artifactId>feeyo-bufferpool</artifactId>
	<version>0.1.0</version>
  </dependency>
	
	
 BufferPool bufferPool = new BucketBufferPool(41943040, 83886080, new int[]{1024, 2048, 4096});
 ByteBuffer theBuf = bufferPool.allocate(500);
 ...
 bufferPool.recycle(theBuf);

```