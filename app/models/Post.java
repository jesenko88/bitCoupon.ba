package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

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
	
	@Embedded
	public String tags;
	
	@Required
	public Date created;
	
	@Required
	@OneToOne
	public User creator;

	public static final String NO_POST_IMAGE = "NO_IMAGE";
	public static final String POST_IMAGE_FOLDER = "blog" + File.separator
												   + "posts" + File.separator; 
	public static Finder<Long, Post> find = new Finder<Long, Post>(Long.class,Post.class);
	
	public Post(String title, String subtitle, String content, String image,
			Date created, User creator, String tags) {	
		if(image == null || image.isEmpty())
			image = NO_POST_IMAGE;
		
		this.title = title;
		this.subtitle = subtitle;
		this.content = content;
		this.image = image;
		this.created = created;
		this.creator = creator;
		this.tags = tags;
	}
	
	public static long createPost(String title, String subtitle, String content, String image,
			Date created, User creator, String tags){
		
		Post post = new Post(title, subtitle, content, image, created, creator, tags);
		post.save();
		return post.id;
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
	
	/**
	 * Method returns all tags of post
	 * as list of string.
	 * Basicly converting from CSV into List.
	 * @return
	 */
	public List<String> getTagsArray(){
		List<String> tags = new ArrayList<String>();
		if(this.tags == null)
			return tags;
		String[] splitted = this.tags.split(",");
		for(String tag: splitted){
			tag.replaceAll(",", "");
			tags.add(tag);
		}		
		return tags;
	}
	
	/**
	 * Creating CSV representation of List<String> tags.
	 * @param splittedValues
	 * @return
	 */
	public static String createCSVTags(String[] splittedValues){
		StringBuilder sb  = new StringBuilder();
		for(String tag: splittedValues){
			if(tag != null && !tag.isEmpty())
				sb.append(tag).append(",");
		}
		if(sb.length() < 1)
			return null;
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	/**
	 * Method goes through list of all posts
	 * and returns list of posts that contains
	 * tag sent as parameter.
	 * @param tag
	 * @return
	 */
	public static List<Post> findByTag(String tag){
		List<Post> byTag = new ArrayList<Post>();
		List<Post> all = find.all();
		for(Post post: all){
			boolean contains = false;
			List<String> tags = post.getTagsArray();
			for(String current: tags){
				if(current.equalsIgnoreCase(tag)){
					contains = true;
					break;
				}
			}
			if(contains)
				byTag.add(post);
		}
		return byTag;
	}
}
