package models.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import models.User;
import play.data.validation.Constraints.*;
import play.db.ebean.Model;

@Entity
public class Post extends Model {

	@Id
	public long id;
	
	@Required
	public String title;
	
	@Required
	public String subtitle;
	
	@Column(columnDefinition = "TEXT")
	public String content;
	
	public String image;
	
	@Required
	public Date created;
	
	@Required
	@OneToOne
	public User creator;

	public static final String NO_POST_IMAGE = "NO_IMAGE";
	public static Finder<Long, Post> find = new Finder<Long, Post>(Long.class,Post.class);
	public Post(String title, String subtitle, String content, String image,
			Date created, User creator) {	
		if(image == null || image.isEmpty())
			image = NO_POST_IMAGE;
		
		this.title = title;
		this.subtitle = subtitle;
		this.content = content;
		this.image = image;
		this.created = created;
		this.creator = creator;
	}
	
	public static void createPost(String title, String subtitle, String content, String image,
			Date created, User creator){
		
		new Post(title, subtitle, content, image, created, creator).save();
	}
	
	public static void updatePost(Post post){
		post.update();
	}
	
	public static void deletePost(long id){
		find.byId(id).delete();
	}
	
	public static Post find(long id){
		return find.byId(id);
	}
	
	/**
	 * Method gets list of all posts and returns it.
	 * Since its better to use if/else in cases if has
	 * more chances to happen,  its checking if list is
	 * not null. 
	 * Only in case there are no posts at all, this if 
	 * condition won't pass.
	 * @return
	 */
	public static List<Post> all(){
		List<Post> all = find.all();
		if(all != null)
			return all;
		return new ArrayList<Post>();
	}
	
}
