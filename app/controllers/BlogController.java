package controllers;

import helpers.FileUpload;

import java.util.Date;
import java.util.UUID;

import models.Post;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.blog.*;;;

public class BlogController extends Controller {

	
	
	
	
	public static Result createPostPage(){
		return ok(createPost.render(new Form<Post>(Post.class)));
	}
	
	public static Result createPost(){
		Form<Post> postForm = Form.form(Post.class).bindFromRequest();
		String title = postForm.data().get("title");
		String subtitle = postForm.data().get("subtitle");
		String content = postForm.data().get("content");
		String picturePath = Post.POST_IMAGE_FOLDER;
		String picture = FileUpload.imageUpload(picturePath);
		Date created = new Date();
		User creator = Sesija.getCurrentUser(ctx());
		
		Post.createPost(title, subtitle, content, picture, created, creator);
		
		return TODO;
	}
}
