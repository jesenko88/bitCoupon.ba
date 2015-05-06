package controllers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

import play.Logger;
import play.Play;


public class ImageController {
		
	
	public static Cloudinary cloudinary = new Cloudinary(Play.application().configuration().getString("cloudinary_environment_variable"));
		
	/**
	 * 
	 * @param image
	 * @return
	 */
	public static String create(File image){

		Map result;
		try {
			result = cloudinary.uploader().upload(image, Cloudinary.emptyMap());
			return (String)result.get("url"); 
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		return null;
	}
	

	/**
	 * Method receives a url of an image from Cloudinary, 
	 * and parses the public_id from it.
	 * @param url String
	 * @return publi_id String
	 */
	public static String getPublicId(String url) {

		return  url.substring((url.lastIndexOf("/")+1), (url.lastIndexOf(".")));
	}
	
	/* Problem sa Cloudinary-em, kada se koristi ova metoda, za svaki reload page-a
	 * napravi se novi file u 'Transformations' na Cloudinary-u. 
	 */
	/**
	 * Receives width, height annd the url of the image from Cloudinary.
	 * Returns the image cropped to the provided size
	 * @param width int
	 * @param height int
	 * @param publicId String
	 * @return url of the cropped image String
	 */
	public static String getSize(int width, int height, String imageUrl){
		
		String url = cloudinary.url().format("jpg")
				  .transformation(new Transformation().width(width).height(height))
				  .generate(getPublicId(imageUrl));
		return url;
	}
	
	/**
	 * Receives a url of an Image on Cloudinary, and deletes it
	 * on the host.
	 * Uses the method getPublicId() to parse the public_id needed
	 * to destroy the image
	 * @param url
	 */
	public static void deleteImage(String url){
		
		try {
			cloudinary.uploader().destroy(getPublicId(url), null);
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
	}

}
