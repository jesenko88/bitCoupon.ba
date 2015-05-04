package controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

import play.Logger;
import play.Play;



public class ImageController {
		
	
	public static Cloudinary cloudinary = new Cloudinary(Play.application().configuration().getString("cloudinary_environment_variable"));
		
	public static String create(File image){
		Map<String, String> params = new HashMap<String, String>();
		params.put("folder", "staticImages/nesto");

		Map result;
		try {
			result = cloudinary.uploader().upload(image, params);
			return (String)result.get("url");
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		return null;
	}
	

	public static String getPublicId(String url) {

		return  url.substring((url.lastIndexOf("/")+1), (url.lastIndexOf(".")));
	}
	
	/**
	 * TODO comment
	 * @param width
	 * @param height
	 * @param publicId
	 * @return
	 */
	public static String getSize(int width, int height, String imageUrl){
		
		String url = cloudinary.url().format("jpg")
				  .transformation(new Transformation().width(width).height(height))
				  .generate(getPublicId(imageUrl));
		return url;
	}
	
	public void deleteImage(String url){
		
		try {
			cloudinary.uploader().destroy(getPublicId(url), null);
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
	}

}
