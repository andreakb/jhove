/**********************************************************************
 * JHOVE - JSTOR/Harvard Object Validation Environment
 * Copyright 2004 by JSTOR and the President and Fellows of Harvard College
 **********************************************************************/

package edu.harvard.hul.ois.jhove.module.wave;

import edu.harvard.hul.ois.jhove.*;
import edu.harvard.hul.ois.jhove.module.WaveModule;
import edu.harvard.hul.ois.jhove.module.iff.Chunk;
import edu.harvard.hul.ois.jhove.module.iff.ChunkHeader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the WAVE Cue Points chunk, which defines cue
 * points in an audio stream.
 *
 * @author Gary McGath
 */
public class CueChunk extends Chunk {

    /**
     * Constructor.
     *
     * @param module   The WaveModule under which this was called
     * @param hdr      The header for this chunk
     * @param dstrm    The stream from which the WAVE data are being read
     */
    public CueChunk(
            ModuleBase module,
            ChunkHeader hdr,
            DataInputStream dstrm) {
        super(module, hdr, dstrm);
    }

    /**
     * Reads a chunk and puts a CuePoints property into the RepInfo object.
     *
     * @return   <code>false</code> if the chunk is structurally invalid,
     *           otherwise <code>true</code>
     */
    public boolean readChunk(RepInfo info) throws IOException {
        WaveModule module = (WaveModule) _module;
        int nPoints = (int) module.readUnsignedInt(_dstream);
        List<Property> points = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; i++) {
            // Get unique ID for cue point
            long dwIdent = module.readUnsignedInt(_dstream);
            // Get position in play order
            long dwPos = module.readUnsignedInt(_dstream);
            // Get chunk ID of referenced chunk ('data' or 'slnt')
            String fccID = module.read4Chars(_dstream);
            // Get offset to start of chunk -- zero if a single
            // Data chunk is used
            long dwChunkStart = module.readUnsignedInt(_dstream);
            // Get offset to start of block containing position
            long dwBlockStart = module.readUnsignedInt(_dstream);
            // Get offset from start of block to cue point.
            // Note from the web page I'm using as a source:
            //    Unfortunately, the WAVE documentation is much too ambiguous, 
            //    and doesn't define what it means by the term "sample offset". 
            //    This could mean a byte offset, or it could mean counting 
            //    the sample points (for example, in a 16-bit wave, every 
            //    2 bytes would be 1 sample point), or it could even mean 
            //    sample frames (as the loop offsets in AIFF are specified). 
            //    Who knows? The guy who conjured up the Cue chunk certainly 
            //    isn't saying. I'm assuming that it's a byte offset, 
            //    like the above 2 fields.
            long dwSampleOffset = module.readUnsignedInt(_dstream);
            Property[] cueProps = new Property[6];
            cueProps[0] = new Property("ID",
                    PropertyType.LONG,
                    dwIdent);
            cueProps[1] = new Property("Position",
                    PropertyType.LONG,
                    dwPos);
            cueProps[2] = new Property("DataChunkID",
                    PropertyType.STRING,
                    fccID);
            cueProps[3] = new Property("ChunkStart",
                    PropertyType.LONG,
                    dwChunkStart);
            cueProps[4] = new Property("BlockStart",
                    PropertyType.LONG,
                    dwBlockStart);
            cueProps[5] = new Property("SampleOffset",
                    PropertyType.LONG,
                    dwSampleOffset);
            points.add(new Property("CuePoint",
                    PropertyType.PROPERTY,
                    PropertyArity.ARRAY,
                    cueProps));
        }
        module.addWaveProperty(new Property("CuePoints",
                    PropertyType.PROPERTY,
                    PropertyArity.LIST,
                    points));
        return true;
    }
}
