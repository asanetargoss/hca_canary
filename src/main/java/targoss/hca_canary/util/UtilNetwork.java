package targoss.hca_canary.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class UtilNetwork {
    // Replacement for RadixCore ByteBufIO.compress
    // Don't trust code from the internet...
    public static byte[] compress(byte[] bytes) {
        try {
          Deflater compressor = new Deflater();
          compressor.setLevel(9);
          compressor.setInput(bytes);
          compressor.finish();
          byte[] scratch = new byte[1024];
          final int intialStreamAlloc = 0;
          ByteArrayOutputStream compressedByteStream = new ByteArrayOutputStream(intialStreamAlloc);
          while (!compressor.finished()) {
              int added = compressor.deflate(scratch);
              compressedByteStream.write(scratch, 0, added);
          }
          compressor.end();
          compressedByteStream.close();
          return compressedByteStream.toByteArray();
        }
        catch (IOException e) {
            return null;
        }
    }

    // Replacement for RadixCore ByteBufIO.decompress
    // Don't trust code from the internet...
    public static byte[] decompress(byte[] bytes) {
        try {
          Inflater decompressor = new Inflater();
          decompressor.setInput(bytes, 0, bytes.length);
          byte[] scratch = new byte[1024];
          final int intialStreamAlloc = 0;
          ByteArrayOutputStream decompressedByteStream = new ByteArrayOutputStream(intialStreamAlloc);
          while (!decompressor.finished()) {
              int added = decompressor.inflate(scratch);
              decompressedByteStream.write(scratch, 0, added);
          }
          decompressor.end();
          decompressedByteStream.close();
          return decompressedByteStream.toByteArray();
        }
        catch (IOException | DataFormatException e) {
            return null;
        }
    }
    
    public static void testDeflate(String s) {
        byte[] in = s.getBytes();
        byte[] compressed = compress(in);
        byte[] decompressed = decompress(compressed);
        System.out.println("testDeflate:\n  in: " + s.toString() + "\n,  compressed: " + new String(compressed) + "\n  decompressed: " + new String(decompressed));
    }
    
    public static void main (String[] args) {
        testDeflate("1");
        testDeflate("\n" + 
                "\n" + 
                "Cum rerum aut alias explicabo repellendus aliquam. Amet beatae quos porro magni at commodi provident quae. Voluptatem libero omnis nulla et qui iure et magni. Quibusdam autem nostrum eius optio est quaerat corrupti. Aut qui quia illum in omnis consequatur. Saepe magnam dolores qui minima quia et incidunt commodi.\n" + 
                "\n" + 
                "Aliquid repudiandae qui amet totam. Laborum excepturi est aperiam impedit pariatur ratione cum veritatis. Qui impedit inventore sit adipisci a ut numquam suscipit. Molestiae aut a tenetur molestiae atque amet eveniet non. Atque est alias iusto occaecati tempora consequatur. Commodi voluptates eveniet sed.\n" + 
                "\n" + 
                "Cum eos reiciendis qui et cupiditate odio. Magnam et dolorum dolore. Facilis aliquid quod est officiis.\n" + 
                "\n" + 
                "Maxime quisquam dolor porro dolores rerum. Asperiores voluptate eveniet perspiciatis dignissimos laboriosam consequatur aut. Vero ex qui voluptatum. Voluptas laboriosam omnis labore deleniti. A suscipit exercitationem voluptatem enim sequi doloribus.\n" + 
                "\n" + 
                "Maxime et ad dolorem enim. Aut sint et quia enim sunt. Modi et officia quas atque perferendis ut aut voluptas. Labore minus similique nam enim ipsa. Dolores officia voluptatem voluptates fugiat libero. Sunt voluptatem praesentium rerum.\n" + 
                "");
    }
}
