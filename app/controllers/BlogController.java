package controllers;

import helpers.FileUpload;

import java.util.ArrayList;
import java.util.Date;

import models.Post;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.admin.blog.*;;;

public class BlogController extends Controller {

	
	public static Result postPage(long id){
		Post currentPost = Post.find(id);
		Html html = new Html(currentPost.content);
		return ok(post.render(html, currentPost));
	}
	
	
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
		String[] tagsArray = postForm.data().get("tags").split(" ");	
		String tags = Post.createCSVTags(tagsArray);
		
		Post.createPost(title, subtitle, content, picture, created, creator,tags);
		
		return TODO;
	}
}
