import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class GoEuro {
	
	public static void main(String[] args) {
		
		/** Check if parameter provided */
		if( args.length <= 0 ) {
			System.out.println("Error, no parameters. Run applikcation: GoEuro [City name]");
			return;
		}
		String cityName = args[0];
		
		/** Get json content from url */
		String url = "http://api.goeuro.com/api/v2/position/suggest/en/"+cityName;
		String urlContent = getUrlContent(url);		
		if( urlContent == null ) {
			System.out.println("City doesn't exists ");
			return;
		}

		/** Transform String to JSONArray */
		JSONArray cityJsonArray = string2JSONArray(urlContent);
		if( cityJsonArray == null ) {
			System.out.println("Parse problem. ");
			return;
		}
		if( cityJsonArray.length() <= 0 ) {
			System.out.println(url + "\nreturns empty JSON array");
			return;
		}
		
		/** Generate csv file from JSONArray */
		String generatedCsvFileName = generteCsvFileFromJSONArray(cityJsonArray, cityName);
		if( generatedCsvFileName != null ) {
			System.out.println("File " + generatedCsvFileName + " generated successfully !");
		}		
	}
	
	public static String getUrlContent(String url) {
		try {
			String returnString = "";
			
	        URL urlObj = new URL(url);
	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(urlObj.openStream()));

	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	returnString += inputLine;
	        }
	        in.close();
			
	        return returnString;
			
		} catch (MalformedURLException e) {
			System.out.println("Error, faild connection to: " + url);
		} catch (IOException e) {
			System.out.println("Error, faild connection to: " + url);
		}
		
		return null;
	}
	
	public static JSONArray string2JSONArray(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			return jsonArray;
		} catch (JSONException e) {
			System.out.println("Content are no JSON array: " + str);
		}
		return null;
	}
	
	public static String generteCsvFileFromJSONArray(JSONArray jsonArray, String fileCoreName) {
		String fileName = fileCoreName + ".csv";
		try {
					
			String DELIMETER = ",";
			String QUOTA = "\"";
			String[] headers = new String[]{"_id","name","type","latitude","longitude"};
			
			FileWriter writer = new FileWriter(fileName);
			
			// add header row
			for( int i=0; i < headers.length; i++) {
				if( i > 0 ) {
					writer.append(DELIMETER);
				}
				writer.append(headers[i]);
			}						
			String[] values;		
	        for( int i=0; i < jsonArray.length(); i++) {

	        	values = new String[5];
	        	
	        	JSONObject jsonObject = jsonArray.getJSONObject(i);
	        	
	        	if( jsonObject.has("_id") ) {
	        		values[0] = jsonObject.getInt("_id") + ""; // get String
	        	}
	        	if( jsonObject.has("name") ) {
	        		 String name = jsonObject.getString("name");	 
	        		 values[1] = csvEscapeString(name, QUOTA);
	        	}
	        	if( jsonObject.has("type") ) {
	        		String type = jsonObject.getString("type");
	        		values[2] = csvEscapeString(type, QUOTA);
	        	}
	        	
	        	if( jsonObject.has("geo_position") ) {
	        		JSONObject geoJsonObject = jsonObject.getJSONObject("geo_position");
	        		if( geoJsonObject.has("latitude") ) {
	        			values[3] = geoJsonObject.getDouble("latitude") + "";
	        		}
	        		if( geoJsonObject.has("longitude") ) {
	        			values[4] = geoJsonObject.getDouble("longitude") + "";
	        		}
	        	}
	        	
	        	writer.append("\n");
	        	
				for ( int j = 0 ; j < values.length ; j++) {
					if( j > 0 ) {
						writer.append(DELIMETER);
					}
					writer.append(values[j]);
				}
			}
			
		    writer.flush();
		    writer.close();
		    
		    return fileName;
		} catch (IOException e) {
			System.out.println("Error writing to file");
		} catch (JSONException e) {
			System.out.println("Error processing JSON");
		}
		
		return null;		
	}
	
	public static String csvEscapeString(String str, String quota) {
		str = str.replace("\"", "\"\"");
		str = quota + str + quota;		
		return str;
	}
}
