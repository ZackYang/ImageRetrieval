package imageretrieval;

import static net.semanticmetadata.lire.utils.ImageUtils.cropImage;
import static net.semanticmetadata.lire.utils.ImageUtils.scaleImage;

import java.awt.image.BufferedImage;
//import java.util.List;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import redis.clients.jedis.Jedis;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
/**
 *
 * @author Zack Yang
 */
public class ImageRetrieval {

    /**
     * @param args the command line arguments
     */
	private static Jedis jedis;
	
    public static void main(String[] args) {
        
    	jedis = new Jedis("10.0.0.57", 6379);
    	index();
    }
 
    private static boolean index() {
    	DocumentBuilder builder = DocumentBuilderFactory.getFCTHDocumentBuilder();
    	IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new WhitespaceAnalyzer());
    	try {
    		IndexWriter iw= new IndexWriter(FSDirectory.open(new File("full_index")), config);
    		String record = "";
	    	do {
	    		try {
	    			record = jedis.rpop("queue");
	    			if (record != null && !record.equals("stop")) {
	    				System.out.println(record);
						BufferedImage image = ImageIO.read(new File(record));
						BufferedImage processedImage = cropImage(scaleImage(image,200,300),20, 20, 160, 260);
						Document doc = builder.createDocument(processedImage, record);
						iw.addDocument(doc);
						iw.commit();
	    			}
					Thread.sleep(5);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	} while (record == null || !record.equals("stop"));
	    	iw.close();
	    	System.out.println("Parser closed !");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
}
