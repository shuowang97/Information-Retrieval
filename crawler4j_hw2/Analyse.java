package csci572_hw2;

import java.io.File;
import java.util.regex.Pattern;

public class Analyse {
	
	public static void tester(){
	   String type = "https://www.latimes.com/lat-about-our-ads,0,3875412.htmlstory";
	   System.out.println(type.replaceAll(",", "-"));
	    
	}

	public static void main(String[] args) {
		tester();
	
		
	}
	
	
}
