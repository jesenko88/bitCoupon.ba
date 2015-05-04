package models;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

import play.Logger;
import play.Play;



public class Image {
		
	
	public static Cloudinary cloudinary = new Cloudinary(Play.application().configuration().getString("cloudinary_environment_variable"));
		
	public static String create(File image){
		Map result;
		try {
			result = cloudinary.uploader().upload(image, null);
			return (String)result.get("url");
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		return null;
	}
	

	public static String getPublicId(String url) {

		return  url.substring((url.lastIndexOf("/")+1), (url.lastIndexOf(".")));
	}
	
	public static String getSize(int width, int height, String publicId){
		String url = cloudinary.url().format("jpg")
				  .transformation(new Transformation().width(width).height(height))
				  .generate(publicId);	
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
