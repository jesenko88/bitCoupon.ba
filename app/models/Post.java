package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import play.data.Form;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Model;

@Entity
public class Post extends Model {

	@Id
	public long id;
	
	@Required
	@MinLength(6)
	@MaxLength(165)
	@Pattern(value="^[A-Za-z0-9 ,.!?_]*[A-Za-z0-9][A-Za-z0-9 ,.!?_]*$", 
			message="Title not valid, only letters and numbers alowed.")
	public String title;
	
	@Required
	@MinLength(6)
	@MaxLength(165)
	@Pattern(value = "^[A-Za-z0-9 ,.!?_]*[A-Za-z0-9][A-Za-z0-9 ,.!?_]*$",
			message="Subtitle not valid, only letters and numbers alowed."	)
	public String subtitle;
	
	@Column(columnDefinition = "TEXT")
	@Required
	public String content;
	
	public String image;	

	@Pattern(value = "^[A-Za-z0-9 ,_]*[A-Za-z0-9][A-Za-z0-9 ,_]*$",
			 message="Tags input not valid. Only characters and numbers alowed."
			 		+ " Tags have to be separated with space.")
	public String tags;	
	
	public Date created;	

	@OneToOne
	@NotNull
	public User creator;

	public static final String NO_POST_IMAGE = "NO_IMAGE";
	public static final String POST_IMAGE_FOLDER = "blog" + File.separator
												   + "posts" + File.separator; 
	public static Finder<Long, Post> find = new Finder<Long, Post>(Long.class,Post.class);
	
	/**
	 * Constructor for Post
	 * @param title
	 * @param subtitle
	 * @param content
	 * @param image
	 * @param created
	 * @param creator
	 * @param tags
	 */
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
		
		

	/**
	 * Method for creating post
	 * @param title
	 * @param subtitle
	 * @param content
	 * @param image
	 * @param created
	 * @param creator
	 * @param tags
	 * @return
	 */
	public static long createPost(String title, String subtitle, String content, String image,
			Date created, User creator, String tags){
		
		Post post = new Post(title, subtitle, content, image, created, creator, tags);
		post.save();
		return post.id;
	}
	
	/**
	 * Method which updates post
	 * @param post
	 */
	public static void updatePost(Post post){
		post.update();
	}
	
	/**
	 * Method which delete post from DB
	 * @param id of post
	 */
	public static void deletePost(long id){
		find.byId(id).delete();
	}
	
	/**
	 * Method which finds post by id in DB
	 * @param id of post
	 * @return post
	 */
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
		if(all != null){
			Collections.reverse(all);
			return all;
		}
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
				System.out.println("TAG" +current);
			}
			if(contains)
				byTag.add(post);
		}
		return byTag;
	}
	
	/**
	 * Method returns all tags (without duplicates)
	 * Since tags is just a string, its possible that many
	 * posts have same tag/tags. Converting list of all tags
	 * to set will remove duplicates, and get it back to array list
	 * creates List of tags without duplicates.
	 * @return
	 */
	public static List<String> allTags(){
		List<Post> all = find.all();
		List<String> allTags = new ArrayList<String>();
		for(Post post: all){
			allTags.addAll(post.getTagsArray());
		}
		Set<String> set = new LinkedHashSet<String>(allTags);
		allTags.clear();
		allTags.addAll(set);
		return allTags;
	}
	
	/**
	 * Since getting errors ( not global errors) from form is
	 * complicated to handle in scala this methods do all the work
	 * and returns errors as list of strings of form sent as parameter.
	 * @param form
	 * @return
	 */
	public static List<String> errors(Form<Post> form){
		 Collection<List<ValidationError>> errors = form.errors().values();
		 Iterator<List<ValidationError>> it = errors.iterator();
		 List<String> errorList = new ArrayList<String>();
		 while(it.hasNext()){
			 for(ValidationError ve: it.next()){
				 errorList.add(ve.message());
			 }
		 }
		 return errorList;
	}
}
