package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.util.ArrayList;
import java.util.Date;

import models.Post;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import views.html.admin.blog.*;;;

public class BlogController extends Controller {

	
	public static Result postPage(long id){
		Post currentPost = Post.find(id);
		Html html = new Html(currentPost.content);
		return ok(post.render(html, currentPost));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result createPostPage(){
		return ok(createPost.render(new Form<Post>(Post.class)));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result createPost(){
		Form<Post> postForm = Form.form(Post.class).bindFromRequest();
		if(postForm.hasErrors() || postForm.hasGlobalErrors()){
			Logger.error("Error while editing post"
					+postForm.globalErrors().toString()
					+postForm.errorsAsJson().toString());
			return ok(createPost.render(postForm));
		}		
		String title = postForm.data().get("title");
		String subtitle = postForm.data().get("subtitle");
		String content = postForm.data().get("content");
		String picturePath = Post.POST_IMAGE_FOLDER;
		String picture = FileUpload.imageUpload(picturePath);
		Date created = new Date();
		User creator = Sesija.getCurrentUser(ctx());		
		String[] tagsArray = postForm.data().get("tags").split(" ");	
		String tags = Post.createCSVTags(tagsArray);
		
		long id = Post.createPost(title, subtitle, content, picture, created, creator,tags);
		
		return postPage(id);
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result editPostPage(long id){
		Post post = Post.find(id);
		Form<Post> form = Form.form(Post.class).fill(post);	
		return ok(editPost.render(form, post));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result editPost(long id){
		Form<Post> postForm = Form.form(Post.class).bindFromRequest();
		if(postForm.hasErrors() || postForm.hasGlobalErrors()){
			Logger.error("Error while editing post");
			return editPostPage(id);
		}
		String title = postForm.data().get("title");
		String subtitle = postForm.data().get("subtitle");
		String content = postForm.data().get("content");
		String picturePath = Post.POST_IMAGE_FOLDER;
		String picture = FileUpload.imageUpload(picturePath);		
		
		
		Post editPost = Post.find(id);
		editPost.title = title;
		editPost.subtitle = subtitle;
		editPost.content = content;
		if(picture != null && !picture.isEmpty()){
			editPost.image = picture;
		}		
		editPost.update();
		return postPage(id);
	}
}
