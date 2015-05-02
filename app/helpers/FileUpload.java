package helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.cloudinary.Cloudinary;
import com.google.common.io.Files;

public class FileUpload extends Controller{
	
	static final double MAX_IMAGE_SIZE = 2.5;

	public static String imageUpload() {
		
		String cloudName = Play.application().configuration().getString("cloudinary_name");
		String apiKey = Play.application().configuration().getString("cloudinary_api_key");
		String apiSecret = Play.application().configuration().getString("cloudinary_api_secret");
		
		Map<String, String> config = new HashMap<String, String>();
		config.put("cloud_name", cloudName);
		config.put("api_key", apiKey);
		config.put("api_secret", apiSecret);
		Cloudinary cloudinary = new Cloudinary(config);
		
		MultipartFormData body = request().body().asMultipartFormData();	
		FilePart filePart = body.getFile("picture");
		
		if(filePart == null){
			return null;
		}
		File image = filePart.getFile();
		String extension = filePart.getFilename().substring(
					filePart.getFilename().lastIndexOf(".")).trim();
		if (!extension.equalsIgnoreCase(".jpeg") 
				&& !extension.equalsIgnoreCase(".jpg")
				&& !extension.equalsIgnoreCase(".png")) {
			flash("error", "Only jpg and png");
			return null;
		}
		
		double mbSize = (double) ((image.length() / 1024)  / 1024);
		if (mbSize > MAX_IMAGE_SIZE) {
			flash("error", "Image too large");
			return null;
		}
		
		@SuppressWarnings("rawtypes")
		Map uploadResult = null;
		try {
			uploadResult = cloudinary.uploader().upload(image, Cloudinary.emptyMap());
		} catch (IOException e2) {
			Logger.error(e2.getMessage());
			e2.printStackTrace();
		}
		
		return 	(String) uploadResult.get("url");

	}	
	/**
	 * Method returning extension of file.
	 * @param filePart
	 * @return
	 */
	public static String getExtension(FilePart filePart){
		return filePart.getFilename().substring(
			   filePart.getFilename().lastIndexOf('.')).trim();		
	}
	
	/**
	 * Method which resizes photo sent to method as BufferedImage.
	 * @param image buffered image, photo to be resized
	 * @param height 
	 * @param width
	 * @return	new resized buffered image.
	 */
	public static BufferedImage resize(BufferedImage image, int width, int height){
		return Scalr.resize(image, Method.QUALITY, width, height, Scalr.OP_ANTIALIAS);		
		
	}
	
	/**
	 * Method confirming that file user wants to upload is valid 
	 * image file. It checks format of picture and its size.
	 * TODO edit this method so we can handle exceptions in other
	 * 		classes.
	 * @param filePart
	 * @return
	 */
	public static File confirmImage(FilePart filePart){
		String extension = filePart.getFilename().substring(
				filePart.getFilename().lastIndexOf('.'));
		extension.trim();		
		
		if (!extension.equalsIgnoreCase(".jpeg")
				&& !extension.equalsIgnoreCase(".jpg")
				&& !extension.equalsIgnoreCase(".png")) {
			flash("error", "Image type not valid");
			return null;
		}
		
		File image = filePart.getFile();
		double megabyteSize = (double) ((image.length() / 1024) / 1024);
		if (megabyteSize > 2) {
			flash("error", "Image size not valid");
			return null;
		}
		
		return image;
	}
	
	
	
	/**
	 * Method which copies file. Uses 2 string paths.
	 * First from which path we copy and second to which file we copy.
	 * @param f1 file from we copy
	 * @param f2 file where we copy
	 * @return
	 */
	public static boolean CopyFile(String f1, String f2){
		File from = new File(f1);
		File to = new File(f2);
		try {
			Files.copy(from, to);
			return true;
		} catch (IOException e) {
			Logger.warn("Coping of file failed");
			return false;
		}
	}
}
