package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import play.Logger;
import play.db.ebean.Model;

/**
 *Model represents statistic for each coupon.
 *TODO: Since its statistic and has lot of insertions in
 *		this model, we should use NoSQL DB for this model.
 */
@Entity
public class Statistic extends Model{
	
	@Id
	public long id;

	@OneToOne
	public Coupon coupon;
	
	public int visited;
	
	public int bought;
	
	static final String statsFilePath = "/statistic/admin_statistic/";
	static Finder<Long, Statistic> find = new Finder<Long, Statistic>(Long.class, Statistic.class);
	/**
	 * Constructor for statistic.
	 * At creation all stats are set to 0.
	 * @param coupon
	 */
	public Statistic(Coupon coupon){
		this.coupon = coupon;
		this.visited = 0;
		this.bought = 0;
	}
	
	/**
	 * Create statistic for coupon.
	 * This method should be used at creation of coupon.
	 * @param coupon
	 */
	public static Statistic createStatistic(Coupon coupon){
		Statistic statistic = new Statistic(coupon);
		statistic.save();
		return statistic;
	}
	
	/**
	 * Reset statistic of a single coupon.
	 * @param coupon
	 */
	public static void resetStatistic(Coupon coupon){
		Statistic stats = find.where().eq("coupon", coupon).findUnique();
		stats.visited = 0;
		stats.bought = 0;
		stats.save();
	}
	
	/**
	 * Non static method for increasing visit statistic.
	 * Created to avoid using .save() and similar things
	 * directly in controller.
	 */
	public void visited(){
		this.visited++;
		this.update();		
	}

	/**
	 * Non static method for increasing number of purchases on this coupon.
	 */
	public void bought(int quantity) {
		this.bought += quantity;
		this.update();	
		
	}
	
	public static File createStatisticsFile(){
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
	        HSSFSheet sheet =  workbook.createSheet("FirstSheet"); 
	        String fileName = UUID.randomUUID().toString().replace("-", "") + ".xls";
	        //CREATING FOLDER FOR STATISTICS                
	        new File(statsFilePath).mkdirs();
	        File statistic = new File(statsFilePath +fileName);
	        List<Statistic> all = find.all();
	        
	        //HARDCODED CELLS
	        HSSFRow rowhead=   sheet.createRow((short)0);
	        rowhead.createCell(0).setCellValue("Coupon id");
	        rowhead.createCell(1).setCellValue("Coupon name");
	        rowhead.createCell(2).setCellValue("Visited");
	        rowhead.createCell(3).setCellValue("Bought");
	        
	        //Creating rows for each statistic.        
	        int rowIndex = 1;
	        for(Statistic stat: all){
	        	HSSFRow row=   sheet.createRow(rowIndex);
	        	row.createCell(0).setCellValue(stat.coupon.id);
	            row.createCell(1).setCellValue(stat.coupon.name);
	            row.createCell(2).setCellValue(stat.visited);
	            row.createCell(3).setCellValue(stat.bought);        	
	        }
	        
	        FileOutputStream fileOut = new FileOutputStream(statistic);
	        workbook.write(fileOut);
	        fileOut.close();
	        workbook.close();
	        System.out.println("Your excel file has been generated!");	
	        
	        return statistic;
		} catch (FileNotFoundException e) {
			Logger.error("Statistic file exception ", e );			
		} catch (IOException e) {
			Logger.error("IO exception", e);
		}  
		return null;
	}
}
