/**
 * 
 */
package br.com.tarlis.mov3lets.method.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.tarlis.mov3lets.method.descriptor.Descriptor;
import br.com.tarlis.mov3lets.method.output.json.SubtrajectoryGSON;
import br.com.tarlis.mov3lets.method.output.json.TOGSON;
import br.com.tarlis.mov3lets.model.MAT;
import br.com.tarlis.mov3lets.model.Subtrajectory;

/**
 * @author tarlis
 *
 */
public class JSONOutputter<MO> extends OutputterAdapter<MO> {

	/**
	 * @param filePath
	 */
	public JSONOutputter(String filePath, Descriptor descriptor) {
		super(filePath, descriptor);
	}

	/**
	 * @param filePath
	 */
	public JSONOutputter(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public void write(List<MAT<MO>> trajectories, List<Subtrajectory> movelets) {
		List<Map<String,Object>> classOfTrajectories = new ArrayList<>();
		
		for (MAT<MO> t : trajectories) {			
			Map<String, Object> classOfT = new HashMap<>();
			classOfT.put("tid", t.getTid());
			classOfT.put("label", t.getMovingObject());			
			classOfTrajectories.add(classOfT);
		}
				
		List <SubtrajectoryGSON> subtrajectoryToGSONs = new ArrayList<>();
		
		for (Subtrajectory movelet : movelets) {
			subtrajectoryToGSONs.add(fromSubtrajectory(movelet));	
		}
		
		TOGSON toGSON = new TOGSON(classOfTrajectories, subtrajectoryToGSONs);
		
		try {
			File file = new File(getFilePath() + "moveletsOnTrain.json"); 
			file.getParentFile().mkdirs(); // TODO remove gambia ?
			
			FileWriter fileWriter = new FileWriter(getFilePath() + "moveletsOnTrain.json");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			gson.toJson(toGSON, fileWriter);
			fileWriter.close();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getLocalizedMessage();
			e.printStackTrace();
		}  
	}
	
	public SubtrajectoryGSON fromSubtrajectory(Subtrajectory s){
		
//		int number_of_point = s.getPoints().size();
		
		List<String> features_in_movelet = new ArrayList<>();
		
		int[] list_features = s.getPointFeatures();
		
		for(int i=0; i<= getDescriptor().getAttributes().size(); i++) {
			if(ArrayUtils.contains(list_features, i))				
				features_in_movelet.add(getDescriptor().getAttributes().get(i).getText());
		}
		
		// TODO Features needed???
//		List<HashMap<String, IFeature>> used_features = new ArrayList<>();
//		
//		for(int i=0; i<s.getPoints().size(); i++) {
//			
//			Point point = s.getPoints().get(i);
//			
//			HashMap<String, IFeature> features_in_point = new HashMap<>();
//			
//			for(String feature:features_in_movelet) {
//				features_in_point.put(feature, point.getFeature(feature));
//			}
//			
//			used_features.add(features_in_point);
//		}
//		return new SubtrajectoryGSON(s.getStart(), s.getEnd(), s.getTrajectory().getTid(), 
//				s.getTrajectory().getMovingObject(), s.getFeatures(), s.getPointFeatures(), s.getSplitpoints(), s.getDistances(), s.getBestAlignments(),
//				s.getQuality(), description, s.getData(), used_features);
		
		return new SubtrajectoryGSON(s.getStart(), s.getEnd(), s.getTrajectory().getTid(), 
				s.getTrajectory().getMovingObject().toString(), s.getPointFeatures(), s.getSplitpoints(), 
				s.getDistances(), s.getBestAlignments(), s.getQuality(), s.getPoints());
	}

}
