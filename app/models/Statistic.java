package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.scene.control.Hyperlink;

import javax.persistence.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

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
	
	static final String statsFilePath = "./statistic/admin_statistic/";
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
	 * Returns whole coupon statistics.
	 * @return
	 */
	public static List<Statistic> all(){
		List<Statistic> all = find.all();
		if(all == null)
			all = new ArrayList<Statistic>();
		return all;
	}
	
	/**
	 * Non static method for increasing visit statistic.
	 * Created to avoid using .save() and similar things
	 * directly in controller.
	 */
	public synchronized void visited(){
		this.visited++;
		this.update();		
	}

	/**
	 * Non static method for increasing number of purchases on this coupon.
	 */
	public synchronized void bought(int quantity) {
		this.bought += quantity;
		this.update();	
		
	}
	
	/**
	 * Method creating excel file with statistic and returning created file.
	 * File name is random UUID.
	 * @return
	 */
	public static File createStatisticsFile(){
		//TODO WHOLE APP TO USE ONE HREF.
		final String href = "http://localhost:9000/";
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
	        HSSFSheet sheet =  workbook.createSheet("Statistics"); 
	        String fileName = UUID.randomUUID().toString().replace("-", "") + ".xls";
	        //CREATING FOLDER FOR STATISTICS                
	        new File(statsFilePath).mkdirs();
	        File statistic = new File(statsFilePath +fileName);
	        List<Statistic> all = find.all();
	        
	        //SETTING STYLE
	        HSSFCellStyle style = workbook.createCellStyle();
	        style.setBorderTop((short) 6); 
	        style.setBorderBottom((short) 1); 
	        style.setFillBackgroundColor(HSSFColor.GREY_80_PERCENT.index);
	        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
	        style.setFillPattern(HSSFColor.GREY_80_PERCENT.index);
	        
	        HSSFFont font = workbook.createFont();
	        font.setFontName(HSSFFont.FONT_ARIAL);
	        font.setFontHeightInPoints((short) 10);
	        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        font.setColor(HSSFColor.WHITE.index);	        
	        style.setFont(font);
	        style.setWrapText(true);
	        
	        
	        //HARDCODED CELLS and added style !
	        HSSFRow rowhead=   sheet.createRow((short)0);	       
	        HSSFCell couponId = rowhead.createCell(0);
	        couponId.setCellValue(new HSSFRichTextString("Coupon id"));
	        couponId.setCellStyle(style);
	       
	        HSSFCell couponName = rowhead.createCell(1);
	        couponName.setCellValue(new HSSFRichTextString("Coupon name"));
	        couponName.setCellStyle(style);
	        
	        HSSFCell  visited = rowhead.createCell(2);
	        visited.setCellValue(new HSSFRichTextString("Visited"));
	        visited.setCellStyle(style);
	        
	        HSSFCell bought = rowhead.createCell(3);
	        bought.setCellValue(new HSSFRichTextString("Bought"));
	        bought.setCellStyle(style); 
	        
	        rowhead.setHeight((short) 0);
	        //Creating rows for each statistic. 
	        //Setting hyperlinks on name.
	        int rowIndex = 1;
	        HSSFCell temp;	      
	        for(Statistic stat: all){
	        	HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
	        	HSSFRow row=   sheet.createRow(rowIndex);
	        	row.createCell(0).setCellValue(stat.coupon.id);
	            temp = row.createCell(1);
	            temp.setCellValue(stat.coupon.name);
	            link.setAddress(href + "coupon/"+stat.coupon.id);
	            temp.setHyperlink(link);
	            row.createCell(2).setCellValue(stat.visited);
	            row.createCell(3).setCellValue(stat.bought); 	            
	            rowIndex++;
	        }
	        
	        //rowhead.setRowStyle(style);
	        //auto size columns
	        for(int i=0; i<4; i++){
	        	 sheet.autoSizeColumn((short) i);	        	 
	        }	       
	       
	        FileOutputStream fileOut = new FileOutputStream(statistic);
	        workbook.write(fileOut);
	        fileOut.flush();
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
