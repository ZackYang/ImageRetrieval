package imageretrieval;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.GenericDoubleLireFeature;
import net.semanticmetadata.lire.imageanalysis.SurfFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.BOVWBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

/**
 *
 * @author bruce
 */
public class Advance_search {

    /**
     * @param args the command line arguments
     */
    private static String indexPath="./index";
    private static int numdoc = 10;
    public static boolean isNumeric(String str)  
    {  
        try  
        {  
            int number = Integer.parseInt(str);
            numdoc = number;
        }  
        catch(NumberFormatException nfe)  
        {  
            return false;  
        }  
        return true;  
    }
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String queryImage = "cheetah.jpeg";
        //String datasetPath = "./images";
        //String indexPath = "./index";
        boolean passed = false;
       
        File file = new File(args[0]);
        File indexf= null;
        if(args.length == 2)
            indexf = new File(indexPath);
        else
            indexf = new File(args[1]);
        if(indexf.exists() && indexf.isDirectory() && file.isFile() ){
            try{
                BufferedImage img = ImageIO.read(file); 
                if(args.length == 2){                    
                    numdoc = Integer.parseInt(args[1]);
                    passed = isNumeric(args[1]);
                }else{
                    indexPath=args[1];
                    numdoc = Integer.parseInt(args[2]);
                    passed = isNumeric(args[2]);
                }
            }catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            
        }
        if(!passed){
             System.out.println("Run \"Avance_search <query_image> numofresult\" to search .");
            System.out.println("Run \"Avance_search <query_image> <index directory> numofresult\" to search) ");
            System.exit(1);    
        }
        
        System.out.println("** SURF BoVW");
        BOVWBuilder bovwBuilderSURF = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new SurfFeature(), 4000, 128);
        bovwBuilderSURF.index();
        
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));

        GenericFastImageSearcher gfisSURF = new GenericFastImageSearcher(numdoc, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF
                + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader);
       
        SurfDocumentBuilder SurfBuilder = new SurfDocumentBuilder();
      
        Document docSURF = bovwBuilderSURF.getVisualWords(SurfBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
       
        ImageSearchHits hitsSURF = gfisSURF.search(docSURF, reader);
  
        System.out.println("SURF results:");
        for(int i= 0; i < hitsSURF.length(); i++){
            String fileName = hitsSURF.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
            System.out.println(i + ": " + hitsSURF.score(i) + " ~ " + fileName);
        }
       
    }
    
}
