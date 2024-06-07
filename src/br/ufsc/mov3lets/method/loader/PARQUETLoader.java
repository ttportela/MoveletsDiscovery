package br.ufsc.mov3lets.method.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

/**
 * The Class CSVLoader.
 *
 * @param <T> the generic type
 */
public class PARQUETLoader<T extends MAT<?>> implements LoaderAdapter<T> {

	/**
	 * Overridden method. 
	 * @see br.com.tarlis.mov3lets.method.loader.LoaderAdapter#loadTrajectories(java.lang.String, br.com.tarlis.mov3lets.method.structures.descriptor.Descriptor).
	 * 
	 * @param file
	 * @param descriptor
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		
		List<MAT<String>> trajectories = new ArrayList<MAT<String>>();
		// IF MO type is String:
//		MO mo = new MO();
		MAT<String> mat = new MAT<String>();

		file += ".parquet";
		
		Path path = new Path(file);
		ParquetFileReader reader = ParquetFileReader.open(HadoopInputFile.fromPath(path, new Configuration()));
		MessageType schema = reader.getFooter().getFileMetaData().getSchema();

        PageReadStore pages;
        
        while ((pages = reader.readNextRowGroup()) != null) {
            long rows = pages.getRowCount();
            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            RecordReader recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));

            for (int i = 0; i < rows; i++) {
                SimpleGroup simpleGroup = (SimpleGroup) recordReader.read();
                
                int tid = Integer.parseInt(simpleGroup.getValueToString(descriptor.getIdFeature().getOrder()-1, 0));
                if (mat.getTid() != tid) {
    				mat = new MAT<String>();
    				mat.setTid(tid);
    				trajectories.add(mat);

    				// Can use like this:
//    				mo = (MO) new MovingObject<String>(label);
    				// OR -- this for typing String:
    				String label = simpleGroup.getValueToString(descriptor.getLabelFeature().getOrder()-1, 0);
    				mat.setMovingObject(label);
    			}
                
             // For each attribute of POI
    			Point poi = new Point();	
    			poi.setTrajectory(mat);
    			for (AttributeDescriptor attr : descriptor.getAttributes()) {

    				if (attr.getType().startsWith("composite_") || attr.getType().startsWith("composite2_")) {
    					String value = simpleGroup.getValueToString(attr.getOrder()-1, 0) 
    							+ " " + simpleGroup.getValueToString(attr.getOrder(), 0);
    					poi.getAspects().add(instantiateAspect(attr, value));
    				} else if (attr.getType().startsWith("composite3_")) {
    					String value = simpleGroup.getValueToString(attr.getOrder()-1, 0) 
    							+ " " + simpleGroup.getValueToString(attr.getOrder(), 0) 
    							+ " " + simpleGroup.getValueToString(attr.getOrder()+1, 0);
    					poi.getAspects().add(instantiateAspect(attr, value));
    				} else 
    					poi.getAspects().add(instantiateAspect(attr, simpleGroup.getValueToString(attr.getOrder()-1, 0)));
    				
    			}
    			mat.getPoints().add(poi);
                
            }
        }
        reader.close();

		return (List<T>) trajectories;
	}

}
