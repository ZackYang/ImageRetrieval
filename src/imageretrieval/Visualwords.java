package imageretrieval;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import static net.semanticmetadata.lire.utils.ImageUtils.cropImage;
import static net.semanticmetadata.lire.utils.ImageUtils.scaleImage;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
public class Visualwords {

    private static String index="./images_index";
    private final static int numberofThread = 16;
    private static String datasetPath = "./images";
    private static String indexPath = "./index";
    int numClusters=2000;
    public static void setUp(String dirName, String indexName){
        datasetPath = dirName;
        indexPath = indexName;
    }
    public static void indexFiles(ArrayList<String> images)throws IOException{
        ChainedDocumentBuilder documentBuilder = new ChainedDocumentBuilder();
        documentBuilder.addBuilder(new SurfDocumentBuilder());
        documentBuilder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);//, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
  
        for(String identifier: images){
            try{
                BufferedImage img = ImageIO.read(new File(identifier));
                BufferedImage processedImage = cropImage(scaleImage(img,200,300),20, 20, 160, 260);
                Document doc = documentBuilder.createDocument(processedImage, identifier);
                iw.addDocument(doc);
               
            }catch(IOException e){
                System.out.println(e);
            }
        }
        iw.close();
        System.out.println("Finished indexing");
        ParallelIndexer parallelIndexer = new ParallelIndexer(numberofThread, indexPath, datasetPath, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new SurfDocumentBuilder());
            }
        };
        parallelIndexer.run();

        System.out.println("Finished clustering");
    }
    public static void main(String[] args)throws IOException{
        boolean passed = false;
        if (args.length == 2) {
            File f = new File(args[0]);
            System.out.println("Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory()){
                    passed = true;
            }
        }
        if (!passed) {
            System.out.println("Run \"Visualwords <directory> <index directory>\" to index files of a directory.");
            System.exit(1);
        }
        setUp(args[0], args[1]);
        ArrayList<String>images = new ArrayList();
        File directory = new File(datasetPath);           
        String[] extensions = new String[] {"jpg", "jpeg"};
        List<File> files = (List<File>)org.apache.commons.io.FileUtils.listFiles(directory, extensions, true);
        for(File file: files){
            images.add(file.getCanonicalPath());
        }
       
        indexFiles(images);    
                
    }
}
