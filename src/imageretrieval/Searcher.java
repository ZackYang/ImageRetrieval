package imageretrieval;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.Gabor;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import static net.semanticmetadata.lire.utils.ImageUtils.cropImage;
import static net.semanticmetadata.lire.utils.ImageUtils.scaleImage;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Yi Yang
 
 * Date: 4.11.15
 */
public class Searcher {
    private static int numdoc = 5;
    private ImageSearcher imageSearcher = null;
    private IndexReader ir = null;
    
    public Searcher() {
    	try {
			createSearcher();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void createSearcher() throws IOException {
        ir = DirectoryReader.open(FSDirectory.open(new File("./full_index")));
        imageSearcher = new GenericFastImageSearcher(numdoc, ColorLayout.class);
        // searching with a image file ...
    }
    
    public String[] run(String filePath) {
        ImageSearchHits hits;
		try {
			hits = imageSearcher.search(processImage(filePath), ir);
			String[] files = new String[hits.length()];
	        for (int i = 0; i < hits.length(); i++) {
	            String fileName = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
	            System.out.println(hits.score(i) + ": \t" + fileName);
	            files[i] = hits.score(i) + ": \t" + fileName;
	        }
	        return files;
		} catch (IOException e) {
			System.out.println("........................");
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			System.out.println("........................");
			e.printStackTrace();
			return null;
		}
    }
    
    public BufferedImage processImage(String filePath) {
        BufferedImage processedImage= null;
        boolean passed = false;

            File f = new File(filePath);
            if (f.exists()) {
                    try {
                        BufferedImage img = ImageIO.read(f);
                        processedImage = cropImage(scaleImage(img,200,300),20, 20, 160, 260);
                        passed = true;
                    }catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

            }
        if (!passed) {
            System.out.println("The argument format is not correct.");
            System.out.println("Run \"Searcher <query image> -flag numofoutputresult\" to search for <query image>.");
            System.exit(1);
        }
        return processedImage;
    }
  
}
