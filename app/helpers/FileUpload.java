package helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.google.common.io.Files;

public class FileUpload extends Controller{

	public static final String DEFAULT_IMAGE = "images" + File.separator + "default_image.jpg";
	public static final double MAX_IMAGE_SIZE = 2.5;
	public static final String IMAGES_FOLDER = "." + File.separator + "public"
			+ File.separator + "images" + File.separator;
	/**
	 * Method for uploading PHOTOS on our website.
	 * As parameter receives name of sub folder in our public/images folder.
	 * 
	 * @param subFolder - sub folder name
	 * @return - String path we use to Asset. ( one we use in creation of coupon)
	 */
	public static String imageUpload(String subFolder) {
		System.out.println("request.body.as ...");
		MultipartFormData body = request().body().asMultipartFormData();

		final String savePath = "." + File.separator + "public"
				+ File.separator + "images" + File.separator + subFolder
				+ File.separator;

		FilePart filePart = body.getFile("picture");
		//Checking if file exists.
		if(filePart == null){
			Logger.debug("File part is null");
			return null;
		}
		File image = filePart.getFile();
		String extension = filePart.getFilename().substring(
				filePart.getFilename().lastIndexOf('.'));
		extension.trim();

		if (!extension.equalsIgnoreCase(".jpeg")
				&& !extension.equalsIgnoreCase(".jpg")
				&& !extension.equalsIgnoreCase(".png")) {
			flash("error", "Image type not valid");
			return null;
		}

		double megabyteSize = (double) ((image.length() / 1024) / 1024);
		if (megabyteSize >MAX_IMAGE_SIZE) {
			Logger.debug("Image size not valid ");
			flash("error", "Image size not valid");
			return null;
		}

		try {
			File profile = new File(savePath + UUID.randomUUID().toString()
					+ extension);
			
			//Trying to resize img.
			BufferedImage img;
			try{
				img = ImageIO.read(image);
				BufferedImage resizedImg = resize(img, 600, 400);
				ImageIO.write(resizedImg, "jpg", image);
			}catch(IOException e1){
				Logger.error("Failed to resize image: " +profile.getPath());
			}
			
			Files.move(image, profile);
			// Path for Assets.to()
			String assetsPath = "images" + File.separator + subFolder
					+ File.separator + profile.getName();
			
			//Trying to resize image.
			
			return assetsPath;
			// Finally creating coupon.

		} catch (IOException e) {
			Logger.debug("Failed to move file");
			e.printStackTrace();
			return null;
		}

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
}
