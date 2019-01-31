package dpf.sp.gpinf.indexer.process.task;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dpf.sp.gpinf.indexer.process.task.photodna.PhotoDNA;
import gpinf.dev.data.EvidenceFile;

public class PhotoDNATask extends AbstractTask{
    
    private Logger LOGGER = LoggerFactory.getLogger(PhotoDNATask.class);
    
    public static final int HASH_SIZE = 144;
    
    public static final String PHOTO_DNA = "photoDNA";
    
    private PhotoDNA photodna;
    
    @Override
    public void init(Properties confParams, File confDir) throws Exception {
        PhotoDNA.setLibPath(new File("E:\\3.3 master_PhotoDNAwithVideo\\PhotoDNA"));
        photodna = new PhotoDNA();
    }

    @Override
    public void finish() throws Exception {
        
    }

    @Override
    protected void process(EvidenceFile evidence) throws Exception {
        
        if(!evidence.getMediaType().getType().equals("image"))
            return;
        
        BufferedImage img = null;
        byte[] data = null;
        
        //try (InputStream is = evidence.getBufferedStream()){
        try (InputStream is = new ByteArrayInputStream(evidence.getThumb())){
            img = ImageIO.read(is);
            
            if(img.getRaster() == null)
                throw new IOException("No raster for image");
            
            data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            
        }catch(Throwable e) {
            e.printStackTrace();
            LOGGER.info("Error loading image " + evidence.getPath() + " " + e.toString());
            evidence.setExtraAttribute("photodna_exception", e.toString());
            return;
        }
        
        BigInteger a = new BigInteger("", 32);
        byte[] hash = new byte[64];
        int ret = photodna.ComputeShort(data, img.getWidth(), img.getHeight(), 0, 0, hash);
        
        if(ret == 0) {
            String hashStr = new String(Hex.encodeHex(hash, false));
            evidence.setExtraAttribute(PHOTO_DNA, hashStr);
        }else {
            LOGGER.info("Error computing photoDNA for " + evidence.getPath());
            evidence.setExtraAttribute("photodna_error", ret);
        }
        
    }
    
}