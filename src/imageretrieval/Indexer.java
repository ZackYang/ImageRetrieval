package imageretrieval;



import static net.semanticmetadata.lire.utils.ImageUtils.cropImage;
import static net.semanticmetadata.lire.utils.ImageUtils.scaleImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.LuceneUtils;
/**
 * @author Bohong Xu
 * Date: 4.11.15
 */
public class Indexer {
    public static void main(String[] args) throws IOException {
        boolean passed = false;
        if (args.length == 2) {
            File f = new File(args[0]);
            System.out.println("Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory()){
                if(args[1].equals("-color") || args[1].equals("-texture") ||  args[1].equals("-combine") || args[1].equals("-shape"))
                    passed = true;
            }
        }
        if (!passed) {
            System.out.println("No directory given as first argument.");
            System.out.println("Run \"Indexer <directory> -flag\" to index files of a directory.");
            System.exit(1);
        }
    
        ArrayList<String>images = new ArrayList();
        File directory = new File(args[0]);           
        String[] extensions = new String[] {"jpg", "jpeg"};
        List<File> files = (List<File>)org.apache.commons.io.FileUtils.listFiles(directory, extensions, true);
        for(File file: files){
            images.add(file.getCanonicalPath());
        }
                
       
        // Creating a FCTH document builder and indexing all files.
        DocumentBuilder builder=null;
         // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
                new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        IndexWriter iw = null;
        if(args[1].equals("-color")){
             builder = DocumentBuilderFactory.getColorLayoutBuilder();
             iw= new IndexWriter(FSDirectory.open(new File("color_index")), conf);
        }
        else if(args[1].equals("-combine")){
            builder = DocumentBuilderFactory.getFCTHDocumentBuilder();
            iw= new IndexWriter(FSDirectory.open(new File("combine_index")), conf);
        }else if(args[1].equals("-shape")){
            builder = DocumentBuilderFactory.getEdgeHistogramBuilder();
            iw = new IndexWriter(FSDirectory.open(new File("shape_index")), conf);
        }
        else{ 
           
            builder = DocumentBuilderFactory.getGaborDocumentBuilder();
            iw = new IndexWriter(FSDirectory.open(new File("texture_index")), conf);
        }
 
        // Iterating through images building the low level features
        for (String imageFilePath: images ) {
           
            //System.out.println("Indexing " + imageFilePath);
            try {
                BufferedImage originImage = ImageIO.read(new File(imageFilePath));
                BufferedImage processedImage = cropImage(scaleImage(originImage,200,300),20, 20, 160, 260);
                Document document;
                
                 document = builder.createDocument(processedImage, imageFilePath);
                
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing "+ imageFilePath);
                e.printStackTrace();
            }
        }
        // closing the IndexWriter
        iw.close();
        System.out.println("Finished indexing.");
    }
}