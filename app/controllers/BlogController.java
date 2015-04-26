package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import models.Post;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
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
	
	
	public static Result blog(){		
		return ok(blog.render(Post.all()));
	}	
	
	public static Result search(String q, String search_param){
		List<Post> search;
		System.out.println("Query is: " +q +" and param is " +search_param);
		if(search_param.equalsIgnoreCase("tags")){
			search = Post.findByTag(q);
		}else{
			search = Post.find.where().
					ilike("title","%" + q +"%").findList();	
			System.out.println("Search size: "+search.size());
		}
		return ok(blog.render(search));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result createPost(){
		Form<Post> postForm = Form.form(Post.class).bindFromRequest(); 
		if(postForm.hasErrors() || postForm.hasGlobalErrors()){
			Logger.error("Error while creating post"
					+"Global errors:"+postForm.globalErrors().toString()
					+"Errors"+postForm.errorsAsJson().toString());
			return ok(createPost.render(postForm));
		}		
		String title = postForm.data().get("title");
		String subtitle = postForm.data().get("subtitle");
		String content = postForm.data().get("content");
		String picturePath = Post.POST_IMAGE_FOLDER;
		String picture = FileUpload.imageUpload(picturePath);
		Date created = new Date();
		User creator = Sesija.getCurrentUser(ctx());
		String tags = postForm.data().get("tags");
		

		content = content.replaceAll("(?i)<(/?script[^>]*)>", "");	
		

		System.out.println(content);
		
		System.out.println(tags);		
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
		
		content = content.replaceAll("(?i)<(/?script[^>]*)>", "");		
		System.out.println(content);
		
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
